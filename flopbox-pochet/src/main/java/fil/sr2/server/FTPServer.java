package fil.sr2.server;

/**
 * Classe permettant de stocker les informations d'un serveur FTP
 *
 * @author pochet
 */
public class FTPServer {

    private String address;
    private int port;

    /**
     * Constructeur de FTPServer
     *
     * @param address Adresse (URI) du serveur FTP
     * @param port    Port du serveur FTP
     */
    public FTPServer(String address, int port) {
        this.address = address;
        this.port = port;
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
     * Méthode permettant d'effectuer un toString d'un serveur FTP
     *
     * @return String
     */
    @Override
    public String toString() {
        return "FTPServer{" +
                "address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
