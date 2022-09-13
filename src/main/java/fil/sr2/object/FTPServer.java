package fil.sr2.object;

/**
 * Classe permettant de stocker les informations d'un serveur FTP
 *
 * @author pochet
 */
public class FTPServer {

    private String username;
    private String password;
    private String address;
    private int port;

    /**
     * Constructeur de FTPServer
     *
     * @param username Nom d'utilisateur du serveur FTP
     * @param password Mot de passe du serveur FTP
     * @param address  Adresse (URI) du serveur FTP
     * @param port     Port du serveur FTP
     */
    public FTPServer(String username, String password, String address, int port) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.port = port;
    }

    /**
     * Constructeur de FTPServer
     *
     * @param username Nom d'utilisateur du serveur FTP
     * @param password Mot de passe du serveur FTP
     */
    public FTPServer(String username, String password) {
        this.username = username;
        this.password = password;
        this.address = null;
        this.port = 0;
    }

    /**
     * Méthode retournant l'adresse d'un serveur FTP
     *
     * @return String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Méthode permettant de modifier l'adresse (URI) d'un serveur FTP
     *
     * @param address Nouveau URI du serveur
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Méthode retournant le port d'un serveur FTP
     *
     * @return Integer
     */
    public int getPort() {
        return port;
    }

    /**
     * Méthode permettant de modifier le port d'un serveur FTP
     *
     * @param port Nouveau port du serveur
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Méthode retournant le nom d'utilisateur d'un serveur FTP
     *
     * @return String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Méthode permettant de modifier le nom d'utilisateur d'un serveur FTP
     *
     * @param username Nouveau nom d'utilisateur
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Méthode retournant le mot de passe d'un serveur FTP
     *
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Méthode permettant de modifier le mot de passe d'un serveur FTP
     *
     * @param password Nouveau mot de passe
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Méthode permettant d'effectuer un toString d'un serveur FTP
     *
     * @return String
     */
    @Override
    public String toString() {
        return "FTPServer{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }

    /**
     * Méthode permettant de vérifier si le FTPServer est complet ou non
     *
     * @return vrai si c'est tous les attributs ne sont pas nuls sinon faux
     */
    public boolean isConnectable() {
        return username != null && password != null && address != null && port != 0;
    }
}
