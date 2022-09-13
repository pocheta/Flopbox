package fil.sr2.object;

/**
 * Classe ServerFile permettant de récupérer les FTPFile transmis par le serveur Flopbox
 *
 * @author pochet
 */
public class ServerFile {
    private String name;
    private boolean isDirectory;
    private int size;
    private String dateAndTime;

    /**
     * Constructeur de la classe ServerFile
     *
     * @param name        Nom du fichier
     * @param isDirectory Boolean permettant de savoir si c'est un répertoire ou non
     * @param size        Taille du fichier
     * @param dateAndTime Date et heure de la dernière modification du fichier.
     */
    public ServerFile(String name, boolean isDirectory, int size, String dateAndTime) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = size;
        this.dateAndTime = dateAndTime;
    }

    /**
     * Méthode permettant de retourner le nom du fichier
     *
     * @return Nom du fichier
     */
    public String getName() {
        return name;
    }

    /**
     * Méthode permettant de définir un nom de fichier
     *
     * @param name Nouveau nom du fichier
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Méthode permettant de retourner si c'est un répertoire ou non
     *
     * @return Vrai si c'est un répertoire sinon faux
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Méthode permettant de définir le statut d'un fichier
     *
     * @param directory Vrai si c'est un répertoire, faux sinon
     */
    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    /**
     * Méthode permettant de retourner la taille d'un fichier
     *
     * @return la taille du fichier
     */
    public int getSize() {
        return size;
    }

    /**
     * Méthode permettant de définir la taille d'un fichier
     *
     * @param size Nouvelle taille du fichier
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Méthode permettant de définir la date et l'heure d'un fichier
     *
     * @return String sous la forme (YYYY-MM-JJ HH:mm:ss)
     */
    public String getDateAndTime() {
        return dateAndTime;
    }

    /**
     * Méthode permettant de définir la date et l'heure d'un fichier
     *
     * @param dateAndTime Nouvelle heure et nouvelle date
     */
    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    /**
     * Méthode permettant d'afficher un ServerFile
     *
     * @return String avec tous les arguments d'un ServerFile
     */
    @Override
    public String toString() {
        return "ServerFile{" +
                "name='" + name + '\'' +
                ", isDirectory=" + isDirectory +
                ", size=" + size +
                ", dateAndTime='" + dateAndTime + '\'' +
                '}';
    }
}