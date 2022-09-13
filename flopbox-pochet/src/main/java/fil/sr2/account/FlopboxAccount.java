package fil.sr2.account;

import fil.sr2.server.FTPServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe permettant de stocker les informations d'un compte Flopbox
 *
 * @author pochet
 */
public class FlopboxAccount {

    private String username;
    private String password;
    private Map<String, FTPServer> serversList;

    /**
     * Constructeur de FlopboxAccount sans liste de serveur
     *
     * @param username Nom de l'utilisateur d'un compte
     * @param password  Mot de passe de l'utilisateur d'un compte
     */
    public FlopboxAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.serversList = new HashMap<>();
    }

    /**
     * Constructeur de FlopboxAccount avec une liste de serveur
     *
     * @param username    Nom de l'utilisateur d'un compte
     * @param password     Mot de passe de l'utilisateur d'un compte
     * @param serversList Liste des serveurs de l'utilisateur
     */
    public FlopboxAccount(String username, String password, Map<String, FTPServer> serversList) {
        this.username = username;
        this.password = password;
        this.serversList = serversList;
    }

    /**
     * Méthode permettant de récupérer le nom d'utilisateur d'un compte
     *
     * @return le nom d'utilisateur en String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Méthode permettant de changer le nom d'utilisateur d'un compte
     *
     * @param username le nouveau nom d'utilisateur en String
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Méthode permettant de récupérer le nom mot de passe d'un compte
     *
     * @return le mot de passe de l'utilisateur en String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Méthode permettant de changer le mot de passe de l'utilisateur d'un compte
     *
     * @param password le nouveau mot de passe en String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Méthode permettant de récupérer la liste des serveurs d'un compte
     *
     * @return une Map avec en key l'alias et en value un FTPServer
     */
    public Map<String, FTPServer> getServersList() {
        return serversList;
    }

    /**
     * Méthode permettant de récupérer un FTPServer en fonction de son alias
     *
     * @param alias Alias du serveur à récupérer
     * @return le FTPServer correspondant à l'alias
     */
    public FTPServer getServerWithAlias(String alias) {
        return serversList.get(alias);
    }

    /**
     * Méthode permettant d'installer une nouvelle liste de serveur pour le d'un compte
     *
     * @param serversList une Map avec en key l'alias et en value un FTPServer
     */
    public void setServersList(Map<String, FTPServer> serversList) {
        this.serversList = serversList;
    }

    /**
     * Méthode permettant d'ajouter un nouveau server a la liste existante pour le compte
     *
     * @param alias  Alias du serveur
     * @param server FTPServer contenant les informations du serveur
     */
    public void addServerToList(String alias, FTPServer server) {
        this.serversList.put(alias, server);
    }

    /**
     * Méthode permettant de supprimer un server a la liste existante pour le compte
     *
     * @param alias Alias du serveur a supprimer
     */
    public void removeServerToList(String alias) {
        this.serversList.remove(alias);
    }

    /**
     * Méthode permettant d'effectuer un toString d'un compte flopbox
     *
     * @return String
     */
    @Override
    public String toString() {
        return "FlopboxAccount{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", serversList=" + serversList +
                '}';
    }
}
