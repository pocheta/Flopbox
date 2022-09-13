package fil.sr2.server;

import fil.sr2.exception.ServerException;
import fil.sr2.account.FlopboxAccount;
import fil.sr2.Main;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Map;
import java.util.Objects;

/**
 * Cette classe permet de gérer les serveurs ainsi que leurs alias pour chaque compte Flopbox
 *
 * @author pochet
 */
@Path("/server/{username}")
public class Server {

    @PathParam("username")
    private String userHeader;
    @HeaderParam("password")
    private String passwordHeader;

    /**
     * Méthode GET permettant de retourner tous les serveurs d'un compte Flopbox
     *
     * @return La liste des serveurs si l'utilisateur a son nom d'utilisateur et son mot de passe valide et que la liste n'est pas vide
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getServers() {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().isEmpty())
            return Response.status(Response.Status.CONFLICT).entity("Aucun serveur ajouté!").build();

        StringBuilder listServers = new StringBuilder();
        for (Map.Entry<String, FTPServer> entry : Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().entrySet()) {
            listServers.append(entry.getKey()).append(" : ").append(entry.getValue().toString()).append("\n");
        }

        return Response.status(Response.Status.OK).entity(listServers.toString()).build();
    }

    /**
     * Méthode POST permettant d'ajouter un nouveau serveur pour un compte Flopbox
     *
     * @param aliasName Alias du serveur a ajouter
     * @param address   Adresse du serveur a ajouter
     * @param port      Port du serveur a ajouter
     * @return Un message indiquant s'il a bien était ajouté ou non
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addServer(@QueryParam("aliasName") String aliasName, @HeaderParam("address") String address, @HeaderParam("port") int port) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().containsKey(aliasName))
            return Response.status(Response.Status.CONFLICT).entity("Alias déjà existant!").build();

        try {
            JSONObject file = new JSONObject(getUser());

            JSONObject account = file.getJSONObject(userHeader);

            JSONObject serverlist;
            if (Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().isEmpty())
                serverlist = new JSONObject();
            else serverlist = account.getJSONObject("serverlist");

            serverlist.put(aliasName, new FTPServer(address, port));
            account.put("serverlist", serverlist);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).addServerToList(aliasName, new FTPServer(address, port));
            return Response.status(Response.Status.OK).entity("Nouveau serveur ajouté sous l'alias : " + aliasName).build();

        } catch (IOException e) {
            throw new ServerException("Erreur lors de l'ajout du serveur FTP sur la plate-forme Flopbox : " + e.getMessage());
        }
    }

    /**
     * Méthode PUT permettant de changer l'URI ou l'Alias d'un serveur pour un compte Flopbox
     *
     * @param alias    Alias du serveur a ajouter
     * @param newAlias Nouveau Alias du serveur
     * @param newUri   Nouveau URI du serveur
     * @return Un message indiquant s'il a bien était modifié ou non
     */
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public Response ChangeAliasOrUri(@QueryParam("alias") String alias, @QueryParam("newAlias") String newAlias, @QueryParam("newUri") String newUri) {
        if (newAlias == null && newUri != null) return changeUri(alias, newUri);
        else if (newAlias != null && newUri == null) return changeAlias(alias, newAlias);
        else return Response.status(Response.Status.CONFLICT).entity("Erreur de paramètre").build();
    }

    /**
     * Méthode permettant de changer l'alias d'un serveur
     *
     * @param alias    Alias du serveur a ajouter
     * @param newAlias Nouveau Alias du serveur
     * @return Un message indiquant s'il a bien était modifié ou non
     */
    public Response changeAlias(String alias, String newAlias) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (!Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().containsKey(alias))
            return Response.status(Response.Status.CONFLICT).entity("Alias non existant!").build();

        if (Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().containsKey(newAlias))
            return Response.status(Response.Status.CONFLICT).entity("Nouveau alias déjà existant!").build();

        try {
            JSONObject file = new JSONObject(getUser());

            JSONObject account = file.getJSONObject(userHeader);

            JSONObject serverlist = account.getJSONObject("serverlist");

            String server = serverlist.getString(alias);

            serverlist.remove(alias);
            serverlist.put(newAlias, server);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();


            FTPServer tmp = Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().get(alias);
            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).removeServerToList(alias);
            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).addServerToList(newAlias, tmp);
            return Response.status(Response.Status.OK).entity("Alias modifié!").build();

        } catch (IOException e) {
            throw new ServerException("Erreur lors du changement d'alias du serveur FTP sur la plate-forme Flopbox : " + e.getMessage());
        }
    }

    /**
     * Méthode permettant de changer l'URI d'un serveur
     *
     * @param alias  Alias du serveur a ajouter
     * @param newUri Nouveau URI du serveur
     * @return Un message indiquant s'il a bien était modifié ou non
     */
    public Response changeUri(String alias, String newUri) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (!Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().containsKey(alias))
            return Response.status(Response.Status.CONFLICT).entity("Alias non existant!").build();

        try {
            JSONObject file = new JSONObject(getUser());
            JSONObject account = file.getJSONObject(userHeader);

            JSONObject serverlist = account.getJSONObject("serverlist");

            String[] server = serverlist.getString(alias).split("'");

            serverlist.remove(alias);

            String port = server[2].split("=")[1];
            int portInt = Integer.parseInt(port.substring(0, port.length() - 1));

            FTPServer ftpServer = new FTPServer(newUri, portInt);
            serverlist.put(alias, ftpServer);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            FTPServer tmp = Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().get(alias);
            tmp.setAddress(newUri);
            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).removeServerToList(alias);
            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).addServerToList(alias, tmp);
            return Response.status(Response.Status.OK).entity("URI modifié!").build();

        } catch (IOException e) {
            throw new ServerException("Erreur lors du changement d'URI du serveur FTP sur la plate-forme Flopbox : " + e.getMessage());
        }
    }

    /**
     * Méthode DELETE permettant de supprimer un alias
     *
     * @param alias Alias du serveur a supprimer
     * @return Un message indiquant s'il a bien était supprimé ou non
     */
    @DELETE
    public Response deleteAlias(@QueryParam("alias") String alias) {
        if (checkConnected())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification incorrect!").build();

        if (!Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).getServersList().containsKey(alias))
            return Response.status(Response.Status.CONFLICT).entity("Alias non existant!").build();

        try {
            JSONObject file = new JSONObject(getUser());

            JSONObject account = file.getJSONObject(userHeader);


            JSONObject serverlist = account.getJSONObject("serverlist");

            serverlist.remove(alias);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            Objects.requireNonNull(retrieveAccount(userHeader, passwordHeader)).removeServerToList(alias);
            return Response.status(Response.Status.OK).entity("Alias supprimé!").build();

        } catch (IOException e) {
            throw new ServerException("Erreur lors de la suppression de l'alias du serveur FTP sur la plate-forme Flopbox : " + e.getMessage());
        }

    }

    /**
     * Méthode retournant un boolean indiquant si l'utilisateur est bien connecté grace au nom d'utilisateur et au mot de passe contenu dans le header
     *
     * @return vrai si les deux parameters sont corrects sinon faux
     */
    private boolean checkConnected() {
        for (FlopboxAccount flopboxAccount : Main.accountList) {
            if (flopboxAccount.getUsername().equals(userHeader) && flopboxAccount.getPassword().equals(passwordHeader)) {
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

    /**
     * Méthode permettant de récupérer le fichier "user.json" pour effectuer des modifications sur le fichier
     *
     * @return Le fichier en string
     */
    private String getUser() {
        if (new File("resources/user.json").exists()) return Main.fileUserToString();
        else return "";
    }
}
