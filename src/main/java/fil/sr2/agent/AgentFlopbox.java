package fil.sr2.agent;

import fil.sr2.exception.JSONException;
import fil.sr2.exception.RunException;
import fil.sr2.object.FTPServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe permet de rediriger chaque fonctionnalité reçue par la main
 * - Lancer un update d'un serveur
 * - Lister le répertoire ./deleted
 * - Supprimer le contenu du répertoire ./deleted
 * - Récupérer un fichier dans le répertoire ./deleted
 *
 * @author pochet
 */
public class AgentFlopbox {

    private static String username = "";
    private static String password = "";

    /**
     * Constructeur de la classe AgentFlopbox
     */
    public AgentFlopbox() {
    }

    /**
     * Méthode run permettant d'effectuer l'exécution et l'update d'un serveur FTP
     *
     * @param pathFile
     * @param pathLocal
     */
    public void run(String pathFile, String pathLocal) throws RunException, JSONException {
        Map<String, FTPServer> ftpServerMap = getUserPassFlopboxAccount(pathFile);
        getServerList(ftpServerMap);

        Map<String, FTPServer> ftpServerMapToUpdate = createDirectory(pathLocal, ftpServerMap);

        if (!ftpServerMapToUpdate.isEmpty()) {
            AgentClient agentClient = new AgentClient(ftpServerMapToUpdate, username, password);

            agentClient.run(pathLocal);
        }
    }

    /**
     * Méthode permettant de lister le contenu du dossier ./deleted
     *
     * @param pathFile Chemin du fichier de config
     * @param alias    Alias du serveur FTP
     */
    public void list(String pathFile, String alias) throws JSONException, RunException {
        Map<String, FTPServer> ftpServerMapToList = setup(pathFile, alias);

        if (!ftpServerMapToList.isEmpty()) {
            AgentClient agentClient = new AgentClient(ftpServerMapToList, username, password);

            agentClient.list();
        }
    }

    /**
     * Méthode permettant de mettre en place la fonction de suppression de tout le dossier ./deleted
     *
     * @param pathFile Chemin du fichier de config
     * @param alias    Alias du serveur FTP
     */
    public void delete(String pathFile, String alias) throws JSONException, RunException {
        Map<String, FTPServer> ftpServerMapToList = setup(pathFile, alias);

        if (!ftpServerMapToList.isEmpty()) {
            AgentClient agentClient = new AgentClient(ftpServerMapToList, username, password);

            agentClient.delete();
        }
    }

    /**
     * Méthode permettant de mettre en place la fonction de récupération d'un fichier dans le ./deleted
     *
     * @param pathFile Chemin du fichier de config
     * @param alias    Alias du serveur FTP
     * @param file     Nom du fichier à récupérer
     * @param path     Chemin de destination du fichier à récupérer sur le serveur FTP
     */
    public void retrieve(String pathFile, String alias, String file, String path) throws JSONException, RunException {
        Map<String, FTPServer> ftpServerMapToList = setup(pathFile, alias);

        if (!ftpServerMapToList.isEmpty()) {
            AgentClient agentClient = new AgentClient(ftpServerMapToList, username, password);

            agentClient.retrieve(file, path);
        }
    }

    /**
     * Méthode permettant de mettre en place la map des alias et des serveurs FTP
     *
     * @param pathFile Chemin du fichier de config
     * @param alias    Alias du serveur FTP
     * @return une map des alias et des serveurs FTP
     */
    private Map<String, FTPServer> setup(String pathFile, String alias) throws JSONException, RunException {
        Map<String, FTPServer> ftpServerMap = getUserPassFlopboxAccount(pathFile);
        getServerList(ftpServerMap);

        Map<String, FTPServer> ftpServerMapToList = new HashMap<>();
        for (String aliasMap : ftpServerMap.keySet()) {
            if (ftpServerMap.get(aliasMap).isConnectable()) ftpServerMapToList.put(aliasMap, ftpServerMap.get(alias));
        }
        return ftpServerMapToList;
    }

    /**
     * Méthode permettant de récupérer les informations à l'intérieur du fichier de config
     *
     * @param pathFile Chemin du fichier de config
     * @return La map des serveurs et des alias du fichier de config
     */
    private static Map<String, FTPServer> getUserPassFlopboxAccount(String pathFile) throws JSONException, RunException {
        String fileres = fileToString(pathFile);
        Map<String, FTPServer> ftpServerMap = new HashMap<>();

        try {
            JSONObject file = new JSONObject(fileres);
            JSONArray serverlist = null;

            for (int i = 0; i < file.names().length(); i++) {
                switch (file.names().get(i).toString()) {
                    case "password":
                        password = file.getString(file.names().getString(i));
                        break;

                    case "username":
                        username = file.getString(file.names().getString(i));
                        break;

                    case "serverlist":
                        serverlist = file.getJSONArray(file.names().getString(i));
                        break;

                    default:
                        System.out.println("Fichier de configuration mal initialisé");
                        break;
                }
            }

            if (serverlist != null) {
                for (int i = 0; i < serverlist.length(); i++) {
                    JSONObject server = serverlist.getJSONObject(i);
                    String alias = "";
                    String username = "";
                    String password = "";

                    for (int j = 0; j < server.names().length(); j++) {

                        switch (server.names().get(j).toString()) {

                            case "alias":
                                alias = server.getString(server.names().get(j).toString());
                                break;

                            case "username":
                                username = server.getString(server.names().get(j).toString());
                                break;

                            case "password":
                                password = server.getString(server.names().get(j).toString());
                                break;

                            default:
                                System.out.println("Fichier de configuration mal initialisé");
                                break;
                        }
                    }

                    FTPServer ftpServer = new FTPServer(username, password);
                    ftpServerMap.put(alias, ftpServer);
                }
            }
        } catch (Exception e) {
            throw new JSONException("Error lors de la récupération JSON : " + e.getMessage());
        }

        return ftpServerMap;
    }

    /**
     * Méthode permettant de récupérer la liste des serveurs enregistrer sur la plate-forme Flopbox
     *
     * @param ftpServerMap Map récupérer par le fichier local contenant des informations du serveur
     */
    private static void getServerList(Map<String, FTPServer> ftpServerMap) throws RunException {
        try {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/server/" + username);

            httpGet.addHeader("password", password);

            CloseableHttpResponse response = client.execute(httpGet);

            String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (response.getStatusLine().getStatusCode() == 200) {
                String[] responseArray = responseMessage.split(":");
                String alias = responseArray[0].substring(0, responseArray[0].length() - 1);
                String[] FTPserverArray = responseArray[1].split(",");


                String address = FTPserverArray[0].split("=")[1].substring(1, FTPserverArray[0].split("=")[1].length() - 1);
                int port = Integer.parseInt(FTPserverArray[1].split("=")[1].substring(0, FTPserverArray[1].split("=")[1].length() - 2));

                if (ftpServerMap.get(alias) != null) {
                    ftpServerMap.get(alias).setAddress(address);
                    ftpServerMap.get(alias).setPort(port);
                }
            }

            client.close();
        } catch (IOException e) {
            throw new RunException("Erreur lors de la récupération des serveurs disponible sur le serveur Flopbox : " + e.getMessage() );
        }
    }

    /**
     * Méthode permettant de créer un répertoire local et de trier les serveurs qui ne sont pas connectable
     *
     * @param pathFile     Chemin de destination local
     * @param ftpServerMap Map contenant les alias des serveurs à créer
     * @return La liste des serveurs dont un dossier a était créer localement
     */
    private static Map<String, FTPServer> createDirectory(String pathFile, Map<String, FTPServer> ftpServerMap) {
        Map<String, FTPServer> ftpServerMapToUpdate = new HashMap<>();

        if (pathFile.charAt(pathFile.length() - 1) != '/') pathFile += "/";

        for (String alias : ftpServerMap.keySet()) {
            if (ftpServerMap.get(alias).isConnectable()) {
                File file = new File(pathFile + alias);
                if (file.mkdir() || file.exists()) {
                    ftpServerMapToUpdate.put(alias, ftpServerMap.get(alias));
                }

            }
        }

        return ftpServerMapToUpdate;
    }

    /**
     * Méthode permettant de transformer un fichier en un String
     *
     * @param filePath Chemin du fichier à transformer
     * @return Le contenu du fichier
     */
    private static String fileToString(String filePath) throws RunException {
        StringBuilder fileres = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = "";
            do {
                line = br.readLine();
                fileres.append(line);
            } while (line != null);
        } catch (IOException e) {
            throw new RunException("Error lors de la lecture du fichier de configuration : " + e.getMessage());
        }
        return fileres.toString();
    }
}