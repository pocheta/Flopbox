package fil.sr2.account;

import fil.sr2.exception.AccountException;
import fil.sr2.Main;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Objects;

/**
 * Cette classe permet de gérer les comptes des utilisateurs
 *
 * @author pochet
 */
@Path("/account")
public class Account {

    /**
     * Méthode GET permettant de récupérer tous les comptes présente sur notre plate-forme FlopBox
     *
     * @return La liste des utilisateurs si présente sinon un message indiquant la non-présence d'utilisateur.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAccount() {
        if (Main.accountList.isEmpty())
            return Response.status(Response.Status.CONFLICT).entity("Aucun utilisateur ajouter!").build();

        StringBuilder listfiles = new StringBuilder();
        for (FlopboxAccount account : Main.accountList) {
            listfiles.append(account.getUsername()).append(" : ").append(account.getPassword()).append("\n");
        }
        return Response.status(Response.Status.OK).entity(listfiles.toString()).build();
    }

    /**
     * Méthode POST permettant d'ajouter un compte sur notre plate-forme Flopbox
     *
     * @param user     Nom de l'utilisateur fourni dans le header de la request
     * @param password Mot de passe fourni dans le header de la request
     * @return Un message indiquant s'il a bien était ajouté ou non
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addAccount(@HeaderParam("username") String user, @HeaderParam("password") String password) {
        if (containsUser(user))
            return Response.status(Response.Status.CONFLICT).entity("Compte utilisateur déjà existant!").build();

        FlopboxAccount newAccount = new FlopboxAccount(user, password);

        try {
            JSONObject file = new JSONObject(getUser());

            JSONObject account = new JSONObject();
            account.put("password", newAccount.getPassword());
            file.put(newAccount.getUsername(), account);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            Main.accountList.add(newAccount);

            return Response.status(Response.Status.OK).entity("Nouveau compte utilisateur ajouté sous le nom : " + user).build();
        } catch (IOException e) {
            throw new AccountException("Erreur lors de l'ajout du compte sur la plate-forme Flopbox : " + e.getMessage());
        }

    }

    /**
     * Méthode DELETE permettant de supprimer un compte sur notre plate-forme Flopbox
     *
     * @param user     Nom de l'utilisateur fourni dans le header de la request
     * @param password Mot de passe fourni dans le header de la request
     * @return Un message indiquant s'il a bien était supprimé ou non
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteAccount(@HeaderParam("username") String user, @HeaderParam("password") String password) {
        if (!containsUser(user))
            return Response.status(Response.Status.CONFLICT).entity("Compte non existant!").build();

        if (containsUserAndPassword(user, password))
            return Response.status(Response.Status.CONFLICT).entity("Mauvais mot de passe pour le compte : " + user).build();

        try {
            JSONObject file = new JSONObject(getUser());
            file.remove(user);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            Main.accountList.remove(retrieveAccount(user, password));
            return Response.status(Response.Status.OK).entity("Compte supprimé!").build();

        } catch (IOException e) {
            throw new AccountException("Erreur lors de la suppression du compte sur la plate-forme Flopbox : " + e.getMessage());
        }
    }


    /**
     * Méthode PUT permettant d'effectuer un changement de mot de passe d'un compte présent sur notre plate-forme Flopbox
     *
     * @param user        Nom de l'utilisateur fourni dans le header de la request
     * @param password    Mot de passe fourni dans le header de la request
     * @param newpassword Nouveau mot de passe fourni dans le header de la request
     * @return Un message indiquant s'il a bien était modifié ou non
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response changePassword(@HeaderParam("username") String user, @HeaderParam("password") String password, @HeaderParam("newpassword") String newpassword) {

        if (!containsUser(user))
            return Response.status(Response.Status.CONFLICT).entity("Compte non existant!").build();

        if (containsUserAndPassword(user, password))
            return Response.status(Response.Status.CONFLICT).entity("Mauvais mot de passe pour le compte : " + user).build();

        try {
            JSONObject file = new JSONObject(getUser());

            JSONObject account = file.getJSONObject(user);
            account.remove("password");
            account.put("password", newpassword);

            PrintWriter out = new PrintWriter("resources/user.json");
            out.println(file);
            out.close();

            Objects.requireNonNull(retrieveAccount(user, password)).setPassword(newpassword);
            return Response.status(Response.Status.OK).entity("Mot de passe changé!").build();

        } catch (IOException e) {
            throw new AccountException("Erreur lors du changement de mot de passe sur la plate-forme Flopbox : " + e.getMessage());
        }
    }

    /**
     * Méthode permettant de retourner un boolean pour indiquer si le compte est présent ou non dans la liste de compte sur la plate-forme Flopbox
     *
     * @param username Nom de l'utilisateur à rechercher dans la liste
     * @return vrai si le nom est présent dans la liste sinon faux
     */
    private boolean containsUser(String username) {
        for (FlopboxAccount flopboxAccount : Main.accountList) {
            if (flopboxAccount.getUsername().equals(username)) return true;
        }
        return false;
    }

    /**
     * Méthode permettant de retourner un boolean pour indiquer si le compte est présent et si le mot de passe est valide ou non dans la liste de compte sur la plate-forme Flopbox
     *
     * @param username Nom de l'utilisateur à rechercher dans la liste
     * @param password Mot de passe à vérifier dans la liste
     * @return vrai si le nom et le mot de passe sont présents dans la liste sinon faux
     */
    private boolean containsUserAndPassword(String username, String password) {
        for (FlopboxAccount flopboxAccount : Main.accountList) {
            if (flopboxAccount.getUsername().equals(username) && flopboxAccount.getPassword().equals(password))
                return false;
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
