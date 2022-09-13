package fil.sr2;

import fil.sr2.agent.AgentFlopbox;
import fil.sr2.exception.JSONException;
import fil.sr2.exception.RunException;

/**
 * Classe main du projet
 *
 * @author pochet
 */
public class Main {

    /**
     * Méthode main permettant de :
     * - Lancer un update d'un serveur
     * - Lister le répertoire ./deleted
     * - Supprimer le contenu du répertoire ./deleted
     * - Récupérer un fichier dans le répertoire ./deleted
     *
     * @param args Arguments du programme
     */
    public static void main(String[] args) throws RunException, JSONException {
        if (args.length < 1) System.out.println("Arguments nécessaire");

        AgentFlopbox agentFlopbox;
        String pathFile;
        String alias;

        switch (args[0]) {
            case "run":
                if (args.length < 3)
                    System.out.println("Fichier de configuration et chemin local requis pour effectuer le téléchargement");
                pathFile = args[1];
                String pathLocal = args[2];
                agentFlopbox = new AgentFlopbox();
                agentFlopbox.run(pathFile, pathLocal);
                break;

            case "-l":
                if (args.length < 3)
                    System.out.println("Fichier de configuration et alias obligatoire");
                pathFile = args[1];
                alias = args[2];
                agentFlopbox = new AgentFlopbox();
                agentFlopbox.list(pathFile, alias);
                break;

            case "-d":
                if (args.length < 3)
                    System.out.println("Fichier de configuration et alias obligatoire");
                pathFile = args[1];
                alias = args[2];
                agentFlopbox = new AgentFlopbox();
                agentFlopbox.delete(pathFile, alias);
                break;

            case "-r":
                String path;
                if (args.length < 4) {
                    System.out.println("Fichier de configuration, alias, nom du fichier a récupérer et chemin de destination obligatoire");
                }

                if (args.length == 4) {
                    path = ".";
                } else path = args[4];

                pathFile = args[1];
                alias = args[2];
                String file = args[3];

                agentFlopbox = new AgentFlopbox();
                agentFlopbox.retrieve(pathFile, alias, file, path);
                break;

            default:
                System.out.println("Arguments incorrect");
                break;
        }
    }
}
