package fil.sr2.client;

import fil.sr2.account.FlopboxAccount;
import fil.sr2.exception.ClientException;
import fil.sr2.Main;
import fil.sr2.server.FTPServer;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Cette classe permet de gérer les clients avec les méthodes pour effectuer des manipulations sur le serveur FTP
 *
 * @author pochet
 */
@Path("/client/{flopboxUser}")
public class Client {

    private FTPClient ftpClient;
    private boolean passiveMode = true;

    @HeaderParam("username")
    private String userHeader;
    @HeaderParam("password")
    private String passwordHeader;
    @HeaderParam("flopboxPassword")
    private String flopboxPassword;
    @PathParam("flopboxUser")
    private String flopboxUser;

    /**
     * Méthode GET permettant de lister tous les fichier et dossier d'un chemin sur le serveur FTP
     *
     * @param alias Alias du serveur FTP
     * @param path  Chemin à lister
     * @return La liste des fichiers/dossiers si l'utilisateur a son nom d'utilisateur et son mot de passe valide et que la liste n'est pas vide
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFTP(@QueryParam("alias") String alias, @QueryParam("path") String path) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        try {
            connect(alias);

            FTPFile[] files = ftpClient.listFiles(path);
            String[] res = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                Timestamp timestamp = new Timestamp(files[i].getTimestamp().getTime().getTime());
                res[i] = files[i].getName() + '\\' + files[i].isDirectory() + '\\' + files[i].getSize() + '\\' + timestamp.toString().split(" ")[0] + '\\' + timestamp.toString().split(" ")[1].substring(0,8);
            }

            disconnect();

            return Response.status(Response.Status.OK).entity(res).build();

        } catch (Exception e) {
            throw new ClientException("Erreur lors de la récupération des fichier sur le serveur FTP : " + e.getMessage());
        }
    }

    /**
     * Méthode GET permettant de télécharger un fichier ou un dossier
     *
     * @param alias         Alias du serveur
     * @param filePath      Chemin du fichier à télécharger
     * @param directoryPath Chemin du dossier à télécharger
     * @return Un message indiquant s'il a bien était téléchargé ou non
     */
    @GET
    @Path("/download")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public Response downloadFTP(@QueryParam("alias") String alias, @QueryParam("filePath") String filePath, @QueryParam("directoryPath") String directoryPath) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (filePath == null && directoryPath != null) return getDirectoryFTP(alias, directoryPath);
        else if (filePath != null && directoryPath == null) return getFileFTP(alias, filePath);
        else return Response.status(Response.Status.OK).entity("Fichier/Dossier inexistant!").build();

    }

    /**
     * Méthode permettant de télécharger un fichier
     *
     * @param alias Alias du serveur
     * @param path  Chemin du fichier à télécharger
     * @return Un message indiquant s'il a bien était téléchargé ou non
     */
    public Response getFileFTP(String alias, String path) {
        if (!path.contains("/")) path = "./" + path;

        Response.ResponseBuilder res;
        connect(alias);

        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            if (contains(path)) {
                InputStream inputStream = ftpClient.retrieveFileStream(path);

                res = Response.ok(inputStream);
            } else {
                res = Response.status(Response.Status.BAD_REQUEST).entity("Fichier non présent");
            }

            disconnect();
        } catch (Exception e) {
            throw new ClientException("Erreur lors de la récupération du fichier pour le téléchargement sur le serveur FTP : " + e.getMessage());
        }

        return res.build();
    }

    /**
     * Méthode permettant de télécharger un dossier
     *
     * @param alias Alias du serveur
     * @param path  Chemin du dossier à télécharger
     * @return Un message indiquant s'il a bien était téléchargé ou non
     */
    public Response getDirectoryFTP(String alias, String path) {
        Response.ResponseBuilder res;
        connect(alias);

        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            String resfiles = directoryToString(path, "");

            res = Response.status(Response.Status.OK).entity(resfiles);

            disconnect();
        } catch (Exception e) {
            throw new ClientException("Erreur lors de la récupération des fichiers/dossiers sur le serveur FTP : " + e.getMessage());
        }

        return res.build();
    }

    /**
     * Méthode permettant d'afficher le contenu d'un dossier
     *
     * @param path       Chemin du dossier
     * @param tabulation Permet d'afficher des tabulations si l'on rentre dans des répertoires pour avoir une arborescence compréhensible
     * @return La liste des fichiers et repertoires en String
     */
    private String directoryToString(String path, String tabulation) {
        StringBuilder resfiles = new StringBuilder();
        try {
            FTPFile[] files = ftpClient.listFiles(path);

            for (FTPFile filelist : files) {

                if (filelist.isDirectory()) {
                    resfiles.append(tabulation).append(filelist.getName()).append("/").append("\n");
                    tabulation += "\t";
                    resfiles.append(directoryToString(path + "/" + filelist.getName(), tabulation));
                } else {
                    resfiles.append(tabulation).append(filelist.getName()).append("\n");
                }
            }
        } catch (IOException e) {
            throw new ClientException("Erreur lors de la récupération des fichiers sur le serveur FTP : " + e.getMessage());
        }

        return resfiles.toString();
    }


    /**
     * Méthode POST permettant d'envoyer un fichier ou un dossier (ZIP) vers le serveur FTP
     *
     * @param alias               Alias du serveur
     * @param path                Chemin de destination du fichier ou du dossier
     * @param uploadedInputStream InputStream du fichier ou du dossier
     * @param fileDetail          String contenant les détails du fichier ou du dossier
     * @return Un message indiquant si le fichier ou le dossier a bien était envoyé vers le serveur ou non
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@QueryParam("alias") String alias, @QueryParam("path") String path, @FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        System.out.println(alias + ", " + path + ", " + uploadedInputStream + ", " + fileDetail + ", " + userHeader + ", " +  passwordHeader + ", " +  flopboxUser + ", " + flopboxPassword);
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        String[] extension = fileDetail.getFileName().split("\\.");

        if (extension[extension.length - 1].equals("zip")) return uploadDirectory(alias, path, uploadedInputStream);
        else return uploadFile(alias, path, uploadedInputStream, fileDetail);
    }

    /**
     * Méthode permettant l'envoie d'un fichier vers le serveur FTP
     *
     * @param alias               Alias du serveur
     * @param path                Chemin de destination du fichier
     * @param uploadedInputStream InputStream du fichier
     * @param fileDetail          String contenant les détails du fichier
     * @return Un message indiquant si le fichier a bien était envoyé vers le serveur ou non
     */
    private Response uploadFile(String alias, String path, InputStream uploadedInputStream, FormDataContentDisposition fileDetail) {
        try {
            Response res;
            connect(alias);

            if (ftpClient.storeFile(path + "/" + fileDetail.getFileName(), uploadedInputStream)) {
                res = Response.status(Response.Status.OK).entity("Upload effectué\n").build();
            } else {
                res = Response.status(Response.Status.BAD_REQUEST).entity("Impossible d'envoyer le fichier/dossier sur le serveur \n").build();
            }

            disconnect();
            return res;

        } catch (IOException e) {
            throw new ClientException("Erreur lors de l'envoie du fichier/dossier sur le serveur FTP : " + e.getMessage());
        }
    }

    /**
     * Méthode permettant l'envoie d'un dossier au format ZIP vers le serveur FTP
     *
     * @param alias               Alias du serveur
     * @param path                Chemin de destination du dossier
     * @param uploadedInputStream InputStream du dossier
     * @return Un message indiquant si le dossier a bien était envoyé vers le serveur ou non
     */
    private Response uploadDirectory(String alias, String path, InputStream uploadedInputStream) {
        try {
            connect(alias);

            File file = Files.createTempFile("output","zip").toFile();
            OutputStream outputStream = new FileOutputStream(file);

            int length;
            byte[] bytes = new byte[1024];
            while ((length = uploadedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
            }

            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();

                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(zipEntry.getName()));

                if (zipEntry.isDirectory()) ftpClient.makeDirectory(path + "/" + zipEntry.getName());
                else ftpClient.storeFile(path + "/" + zipEntry.getName(), inputStream);
            }

            disconnect();
            return Response.status(Response.Status.OK).entity("Upload du dossier effectué\n").build();

        } catch (IOException e) {
            throw new ClientException("Erreur lors de l'envoie du dossier sur le serveur FTP : " + e.getMessage());
        }
    }

    /**
     * Méthode DELETE permettant de supprimer un dossier ou un fichier
     *
     * @param alias Alias du serveur
     * @param path  Chemin du fichier/dossier à supprimer
     * @return Un message indiquant s'il a bien était supprimé ou non
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@QueryParam("alias") String alias, @QueryParam("path") String path) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        try {
            Response res;
            connect(alias);

            if (ftpClient.removeDirectory(path))
                res = Response.status(Response.Status.OK).entity("Dossier supprimer\n").build();
            else {
                if (ftpClient.deleteFile(path))
                    res = Response.status(Response.Status.OK).entity("Fichier supprimer\n").build();
                else
                    res = Response.status(Response.Status.BAD_REQUEST).entity("Impossible de supprimer le fichier/dossier \n").build();
            }

            disconnect();
            return res;

        } catch (Exception e) {
            throw new ClientException("Erreur lors de la suppression du fichier/dossier sur le serveur FTP : " + e.getMessage());
        }

    }

    /**
     * Méthode POST permettant de créer un nouveau répertoire
     *
     * @param alias         Alias du serveur
     * @param path          Chemin de l'endroit ou la création du répertoire sera fait
     * @param directoryName Nom du dossier à créer
     * @return Un message indiquant s'il a bien était créé ou non
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createdir(@QueryParam("alias") String alias, @QueryParam("path") String path, @QueryParam("directoryName") String directoryName) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        try {
            Response res;
            connect(alias);

            if (ftpClient.makeDirectory(path + '/' + directoryName))
                res = Response.status(Response.Status.OK).entity("Dossier créer\n").build();
            else res = Response.status(Response.Status.BAD_REQUEST).entity("Impossible de créer le dossier\n").build();

            disconnect();
            return res;
        } catch (Exception e) {
            throw new ClientException("Erreur lors de la création d'un nouveau répertoire sur le serveur FTP : " + e.getMessage());
        }

    }

    /**
     * Méthode PUT permettant de renommer un répertoire ou de changer le mode entre actif et passif
     *
     * @param mode        Nom du mode à définir (Passif par default)
     * @param alias       Alias du serveur
     * @param path        Chemin du fichier ou dossier à renommer
     * @param newFileName Nouveau nom du fichier/dossier
     * @return Un message indiquant s'il a bien était rename ou si le mode a bien était changé ou non
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response putMethod(@QueryParam("mode") String mode, @QueryParam("alias") String alias, @QueryParam("path") String path, @QueryParam("newName") String newFileName) {
        if (mode == null && (alias != null && path != null && newFileName != null))
            return rename(alias, path, newFileName);
        else if (mode != null) return activeOrPassiveMode(mode);
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Fichier/Dossier inexistant!").build();
    }

    /**
     * Méthode permettant de rename un fichier ou un dossier
     *
     * @param alias       Alias du serveur
     * @param path        Chemin du fichier ou dossier
     * @param newFileName Nouveau nom du fichier ou dossier
     * @return Un message indiquant s'il a bien était rename ou non
     */
    private Response rename(String alias, String path, String newFileName) {
        System.out.println(alias + ", " + path + ", " + newFileName);
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        try {
            Response res;
            connect(alias);

            if (ftpClient.rename(path, newFileName))
                res = Response.status(Response.Status.OK).entity("Fichier/Dossier renommer !\n").build();
            else
                res = Response.status(Response.Status.BAD_REQUEST).entity("Impossible de renommer le fichier/dossier\n").build();

            disconnect();
            return res;
        } catch (Exception e) {
            throw new ClientException("Erreur lors du renommage du fichier/dossier sur le serveur FTP : " + e.getMessage());
        }
    }

    /**
     * Méthode permettant de passer du mode actif au mode passif (Passif par default)
     *
     * @param mode Nom du mode à mettre à jour
     * @return Un message indiquant si le mode a bien était mis à jour ou non
     */
    private Response activeOrPassiveMode(String mode) {
        if (mode.equals("passive")) {
            passiveMode = true;
            return Response.status(Response.Status.OK).entity("Mode passif activé!\n").build();
        } else if (mode.equals("active")) {
            passiveMode = false;
            return Response.status(Response.Status.OK).entity("Mode actif activé!\n").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Paramètre incorrect (mode passif par défaut)!\n").build();
        }
    }

    /**
     * Méthode permettant se connecter au serveur FTP grâce à un alias
     *
     * @param alias Alias du serveur
     */
    private void connect(String alias) {
        FTPServer server = Objects.requireNonNull(retrieveAccount(flopboxUser, flopboxPassword)).getServerWithAlias(alias);

        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server.getAddress(), server.getPort());
            ftpClient.login(userHeader, passwordHeader);

        } catch (IOException e) {
            throw new ClientException("Erreur lors de la connexion au le serveur FTP : " + e.getMessage());
        }

        if (passiveMode) ftpClient.enterLocalPassiveMode();
        else ftpClient.enterLocalActiveMode();

    }

    /**
     * Méthode permettant de se déconnecter du serveur si celui-ci est connecté
     */
    private void disconnect() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                throw new ClientException("Erreur lors de la déconnexion du le serveur FTP : " + e.getMessage());
            }
        }
    }

    /**
     * Méthode permettant de vérifier si le fichier ou dossier passer en paramètre est bien présent sur le serveur FTP
     *
     * @param path Chemin du fichier ou dossier à vérifier
     * @return vrai s'il est présent sinon faux
     */
    private boolean contains(String path) {
        int lastSlash = path.lastIndexOf('/');
        String fileName = path.substring(lastSlash + 1);
        String pathWithoutFile = path.substring(0, lastSlash);

        try {
            FTPFile[] files = ftpClient.listFiles(pathWithoutFile);

            for (FTPFile filelist : files) {
                if (filelist.getName().equals(fileName)) return true;
            }
        } catch (IOException e) {
            throw new ClientException("Erreur lors de la récupération des fichiers sur le serveur FTP : " + e.getMessage());
        }

        return false;
    }

    /**
     * Méthode retournant un boolean indiquant si l'utilisateur est bien connecté grace au nom d'utilisateur et au mot de passe contenu dans le header
     *
     * @return vrai si les deux parameters sont corrects sinon faux
     */
    private boolean checkConnected() {
        for (FlopboxAccount flopboxAccount : Main.accountList) {
            if (flopboxAccount.getUsername().equals(flopboxUser) && flopboxAccount.getPassword().equals(flopboxPassword)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Méthode permettant de retourner le FlopboxAccount correspondant au nom de l'utilisateur et au mot de passe fourni présent dans la liste de compte sur la plate-forme Flopbox
     *
     * @param username Nom de l'utilisateur à rechercher dans la liste
     * @param password Mot de passe à vérifier dans la liste
     * @return Le FlopboxAccount correspondant sinon null
     */
    private FlopboxAccount retrieveAccount(String username, String password) {
        for (FlopboxAccount flopboxAccount : Main.accountList) {
            if (flopboxAccount.getUsername().equals(username) && flopboxAccount.getPassword().equals(password))
                return flopboxAccount;
        }
        return null;
    }

}

//            List<File> files = new ArrayList<>();
//
//            ZipInputStream zin = new ZipInputStream(uploadedInputStream);
//            ZipEntry entry = null;
//            while((entry = zin.getNextEntry()) != null) {
//                File file = new File(entry.getName());
//                FileOutputStream os = new FileOutputStream(file);
//                for (int c = zin.read(); c != -1; c = zin.read()) {
//                    os.write(c);
//                }
//                os.close();
//                files.add(file);
//            }
//
//            for (File f : files) {
//                InputStream targetStream = new FileInputStream(f);
//                if (f.isDirectory()) ftpClient.makeDirectory("coucou2/" + f.getName());
//                else ftpClient.storeFile("coucou2/" + f.getName(), targetStream);
//            }
