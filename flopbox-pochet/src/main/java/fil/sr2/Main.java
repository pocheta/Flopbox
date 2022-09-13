package fil.sr2;

import fil.sr2.account.FlopboxAccount;
import fil.sr2.server.FTPServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Classe main permettant de lancer le serveur
 *
 * @author pochet
 */
public class Main {
    public static final List<FlopboxAccount> accountList = new ArrayList<>();

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/flopbox/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example.rest package
        final ResourceConfig rc = new ResourceConfig().packages("fil.sr2");
        rc.register(MultiPartFeature.class);
        rc.register(JacksonFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args Argument de la fonction Main
     * @throws IOException Exception de la méthode Main
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));

        retrieveUser();

        System.in.read();
        server.shutdownNow();
    }

    /**
     * Méthode permettant, au lancement du serveur, d'ajouter tous les utilisateurs ainsi que leurs informations contenues dans le fichier "user.json" a notre liste de la plate-forme Flopbox
     */
    private static void retrieveUser() {
        String fileres = fileUserToString();

        try {
            JSONObject file = new JSONObject(fileres);
            Iterator<String> keys = file.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (file.get(key) instanceof JSONObject) {
                    JSONObject utilisateur = (JSONObject) file.get(key);
                    FlopboxAccount usertoadd = new FlopboxAccount(key, utilisateur.getString("password"));

                    List<String> containsServerlist = new ArrayList<>();
                    for (int index = 0; index < utilisateur.names().length(); index++) {
                        containsServerlist.add(utilisateur.names().getString(index));
                    }

                    if (containsServerlist.contains("serverlist")) {
                        Map<String, FTPServer> listofserver = new HashMap<>();
                        JSONObject serverlist = (JSONObject) utilisateur.get("serverlist");

                        for (int i = 0; i < serverlist.names().length(); i++) {
                            String alias = serverlist.names().getString(i);

                            String[] server = serverlist.getString(alias).split("'");
                            String address = server[1];
                            String port = server[2].split("=")[1];

                            int portInt = Integer.parseInt(port.substring(0, port.length() - 1));

                            listofserver.put(alias, new FTPServer(address, portInt));
                        }

                        usertoadd.setServersList(listofserver);
                    }
                    accountList.add(usertoadd);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode permettant de transformer le fichier "user.json" en String
     *
     * @return le fichier en String
     */
    public static String fileUserToString() {
        StringBuilder fileres = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("resources/user.json"))) {
            String line = "";
            do {
                line = br.readLine();
                fileres.append(line);
            } while (line != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileres.toString();
    }
}