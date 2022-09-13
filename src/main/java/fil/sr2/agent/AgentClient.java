package fil.sr2.agent;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import fil.sr2.exception.RunException;
import fil.sr2.object.FTPServer;
import fil.sr2.object.ServerFile;
import io.swagger.api.ClientApi;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Cette classe permet de gérer les updates de chaque serveur FTP
 *
 * @author pochet
 */
public class AgentClient {

    private final Map<String, FTPServer> ftpServerMap;
    public static ClientApi api;
    private final String USERNAME;
    private final String PASSWORD;

    /**
     * Constructeur de AgentClient
     *
     * @param ftpServerMap Map des serveurs FTP à update avec leur alias
     * @param username     Username flopbox
     * @param password     Password flopbox
     */
    public AgentClient(Map<String, FTPServer> ftpServerMap, String username, String password) {
        this.ftpServerMap = ftpServerMap;
        this.USERNAME = username;
        this.PASSWORD = password;
        setup();
    }

    /**
     * Méthode permettant de mettre en place les appels au serveur Flopbox
     */
    private void setup() {
        JacksonJsonProvider provider = new JacksonJsonProvider();
        List providers = new ArrayList();
        providers.add(provider);

        api = JAXRSClientFactory.create("http://localhost:8080/flopbox", ClientApi.class, providers);
        Client client = WebClient.client(api);

        ClientConfiguration config = WebClient.getConfig(client);
    }

    /**
     * Méthode permettant de faire une boucle infinie pour mettre à jour un serveur FTP
     *
     * @param path Destination locale pour le téléchargement du contenu d'un serveur FTP
     */
    public void run(String path) throws RunException {
        while (true) {
            try {
                this.update(path);
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RunException("Interruption lors de la mise à jour du serveur : " + e.getMessage());
            }
        }
    }

    /**
     * Méthode permettant de faire l'update d'un serveur (L'initialise si c'est vide sinon fait un update toute les minutes)
     *
     * @param path Destination locale pour le téléchargement du contenu d'un serveur FTP
     */
    private void update(String path) throws RunException {
        for (String alias : ftpServerMap.keySet()) {

            // Récupération des fichiers locaux
            File diretoryAlias = new File(path + "\\" + alias);
            File[] listFiles = diretoryAlias.listFiles();
            List<File> localFiles = Arrays.asList(listFiles);

            // Verification du dossier local, s'il est vide, aucun pull n'a était déjà éffectuer
            if (localFiles.isEmpty()) {
                pull(path + "\\" + alias, "", alias);
            } else {
                // Sinon on update
                updateDirectory(alias, localFiles, "");
            }
        }
    }

    /**
     * Méthode permettant de mettre à jour un répertoire
     *
     * @param alias      Alias du serveur FTP
     * @param localFiles Liste de fichier local
     * @param path       Chemin du serveur FTP
     */
    private void updateDirectory(String alias, List<File> localFiles, String path) throws RunException {
        // Récupération des fichiers FTP
        String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path).toString();
        if (!ftpReceiveFiles.equals("[]")) {
            List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));

            for (File file : localFiles) {
                if (fileContainsInServerList(file.getName(), serverFiles)) {
                    if (file.isDirectory()) {
                        File diretoryAlias = new File(file.getPath());
                        File[] listFiles = diretoryAlias.listFiles();

                        updateDirectory(alias, Arrays.asList(listFiles), path + "/" + file.getName());
                    } else {
                        try {
                            Path pathFile = Paths.get(file.getPath());
                            FileTime fileTime = Files.getLastModifiedTime(pathFile);

                            String date = fileTime.toString().split("T")[0];
                            String time = fileTime.toString().split("T")[1].substring(0, 8);

                            ServerFile serverFile = retrieveServerFile(file.getName(), serverFiles);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date localDate = sdf.parse(date + " " + time);
                            Date ftpDate = sdf.parse(serverFile.getDateAndTime());

                            int result = localDate.compareTo(ftpDate);

                            if (result < 0) {
                                pullFile(alias, path, file, ftpDate);
                            } else if (result > 0) {
                                pushFile(alias, path, file);

                            } else {
                                if (file.getTotalSpace() < serverFile.getSize()) {
                                    pullFile(alias, path, file, ftpDate);
                                } else if (file.getTotalSpace() > serverFile.getSize()) {
                                    pushFile(alias, path, file);
                                }
                            }

                        } catch (IOException | ParseException e) {
                            throw new RunException("Interruption lors de la mise à jour du serveur : " + e.getMessage());
                        }
                    }
                } else {
                    File initialFile = new File(file.getPath());
                    if (path.equals(""))
                        api.addToFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path + "/", initialFile);
                    else
                        api.addToFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path.substring(1, path.length()) + "/", initialFile);
                }
            }

            for (ServerFile serverFile : serverFiles) {
                if (!serverFileContainsInLocalList(serverFile.getName(), localFiles)) {
                    if (path.equals(""))
                        api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, serverFile.getName(), ".deleted/" + serverFile.getName(), null);
                    else
                        api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path.substring(1, path.length()) + "/" + serverFile.getName(), ".deleted/" + serverFile.getName(), null);
                }
            }
        }
    }

    /**
     * Méthode permettant de retourner le ServerFile contenu dans une liste
     *
     * @param name        Nom du fichier à retourner
     * @param serverFiles Liste de ServerFile contenant les fichiers du serveurFTP
     * @return Le serverFile qui a le nom passer en paramètre
     */
    private ServerFile retrieveServerFile(String name, List<ServerFile> serverFiles) {
        for (ServerFile serverFile : serverFiles) {
            if (serverFile.getName().equals(name)) return serverFile;
        }
        return null;
    }

    /**
     * Méthode permettant de récupérer des dossiers / fichiers d'un serveur FTP
     *
     * @param localPath  Chemin de destination local
     * @param serverPath Chemin de destination du serveur FTP
     * @param alias      Alias du serveur FTP
     */
    private void pull(String localPath, String serverPath, String alias) throws RunException {
        String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, serverPath.replace("\\", "/")).toString();
        if (!ftpReceiveFiles.equals("[]")) {
            List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));

            for (ServerFile serverFile : serverFiles) {
                if (serverFile.isDirectory()) {
                    try {
                        Files.createDirectory(Paths.get(localPath + "\\" + serverFile.getName()));
                        if (serverPath.equals(""))
                            pull(localPath + "\\" + serverFile.getName(), serverFile.getName(), alias);
                        else
                            pull(localPath + "\\" + serverFile.getName(), serverPath + "\\" + serverFile.getName(), alias);
                    } catch (IOException e) {
                        throw new RunException("Error lors de la récupération des données du serveur : " + e.getMessage());
                    }
                } else {
                    InputStream inputStream = api.retrieveFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, serverPath.replace("\\", "/") + "/" + serverFile.getName());
                    try {
                        writeLocalFile(inputStream, localPath + "\\" + serverFile.getName());

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date ftpDate = sdf.parse(serverFile.getDateAndTime());
                        Files.setAttribute(Paths.get(localPath), "lastModifiedTime", FileTime.fromMillis(ftpDate.getTime()));
                    } catch (IOException | ParseException e) {
                        throw new RunException("Error lors de la récupération des données du serveur : " + e.getMessage());
                    }
                }
            }

        }
    }

    /**
     * Méthode permettant d'écrire les données d'un fichier du serveur dans un fichier local
     *
     * @param inputStream Données du fichier du serveur
     * @param pathFile    Chemin de destination local
     */
    private void writeLocalFile(InputStream inputStream, String pathFile) throws RunException {
        try {
            File file = new File(pathFile);
            FileOutputStream outputStream = new FileOutputStream(file, false);
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RunException("Error lors de l'écriture dans le fichier local : " + e.getMessage());
        }
    }

    /**
     * Méthode permettant de mettre un fichier précis sur le serveur FTP
     *
     * @param alias Alias du serveur FTP
     * @param path  Chemin de destination pour upload le fichier
     * @param file  Fichier à upload sur le serveur FTP
     */
    private void pushFile(String alias, String path, File file) {
        File initialFile = new File(file.getPath());

        if (path.equals("")) {
            api.removeFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, file.getName());
            api.addToFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, file.getName(), initialFile);
        } else {
            api.removeFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path + "/" + file.getName());
            api.addToFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path + "/" + file.getName(), initialFile);
        }
    }

    /**
     * Méthode permettant de récupérer un fichier précis sur le serveur FTP
     *
     * @param alias Alias du serveur FTP
     * @param path  Chemin de destination pour récupérer le fichier
     * @param file  Fichier local à récupérer sur le serveur FTP
     * @param date  Date du fichier
     * @throws IOException
     */
    private void pullFile(String alias, String path, File file, Date date) throws IOException, RunException {
        String localPath = file.getPath();
        Files.delete(Paths.get(localPath));

        InputStream inputStream = api.retrieveFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, path + "/" + file.getName());
        writeLocalFile(inputStream, localPath);
        Files.setAttribute(Paths.get(localPath), "lastModifiedTime", FileTime.fromMillis(date.getTime()));
    }

    /**
     * Méthode permettant de savoir si une donnée est contenu dans la list de File
     *
     * @param name       Nom du fichier à rechercher
     * @param localFiles Liste de File
     * @return Vrai si le nom est dans la liste sinon faux
     */
    private boolean serverFileContainsInLocalList(String name, List<File> localFiles) {
        for (File file : localFiles) {
            if (file.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Méthode permettant de savoir si une donnée est contenu dans la list de ServerFile
     *
     * @param name        Nom du fichier à rechercher
     * @param serverFiles Liste de ServerFile
     * @return Vrai si le nom est dans la liste sinon faux
     */
    private boolean fileContainsInServerList(String name, List<ServerFile> serverFiles) {
        for (ServerFile serverFile : serverFiles) {
            if (serverFile.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Méthode retournant une liste de ServerFile grace au String en paramètre
     *
     * @param ftpReceiveFiles String donner par le serveur Flopbox contenant tous les fichiers
     * @return liste de ServerFile
     */
    private List<ServerFile> stringToServerFile(String ftpReceiveFiles) {
        List<ServerFile> res = new ArrayList<>();

        for (String f : ftpReceiveFiles.split(",")) {
            f = f.replace(" ", "");
            String[] tab = f.split("\\\\");
            ServerFile serverFile = new ServerFile(tab[0], tab[1].equals("true"), Integer.parseInt(tab[2]), tab[3] + " " + tab[4]);
            res.add(serverFile);
        }
        return res;
    }

    /**
     * Méthode permettant de lister le contenu du dossier ./deleted
     */
    public void list() {
        for (String alias : ftpServerMap.keySet()) {
            String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
            if (!ftpReceiveFiles.equals("[]")) {
                List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
                System.out.println("--- .deleted/");
                for (int i = 0; i < serverFiles.size(); i++) {
                    System.out.println("|   --- " + serverFiles.get(i).getName());
                }
            }
        }
    }

    /**
     * Méthode permettant de supprimer le contenu du dossier ./deleted
     */
    public void delete() {
        for (String alias : ftpServerMap.keySet()) {
            String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
            if (!ftpReceiveFiles.equals("[]")) {
                List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
                for (ServerFile serverFile : serverFiles) {
                    api.removeFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + serverFile.getName());
                }
            }
        }
    }

    /**
     * Méthode permettant de récupérer un fichier du dossier ./deleted
     *
     * @param fileName Nom du fichier à récupérer
     * @param path     Destination du fichier à récupérer
     */
    public void retrieve(String fileName, String path) {
        for (String alias : ftpServerMap.keySet()) {
            String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
            if (!ftpReceiveFiles.equals("[]")) {
                List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
                for (ServerFile serverFile : serverFiles) {
                    if (serverFile.getName().equals(fileName)) {
                        if (path.equals(""))
                            api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + fileName, "../" + fileName, null);
                        else
                            api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + fileName, "../" + path + "/" + fileName, null);
                    }
                }
            }
        }
    }
}
