package fil.sr2;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

/**
 * Cette classe permet de tester la classe Server
 *
 * @author pochet
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerTest {

    private static HttpServer server;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPassword";

    /**
     * Cette méthode permet de créer et de démarrer le serveur
     *
     */
    @BeforeAll
    static public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        WebTarget target = c.target(Main.BASE_URI);

        addAccount();
    }

    /**
     * Cette méthode permet de stopper le serveur
     *
     */
    @AfterAll
    static public void tearDown() throws Exception {
        deleteAccount();
        server.stop();
    }

    /**
     * Test de la méthode GET sans aucun serveur enregistré sur la plate-forme Flopbox
     */
    @Test
    @Order(1)
    public void testGetAnyServer() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/server/" + USERNAME);

        httpGet.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Aucun serveur ajouté!");

        client.close();
    }

    /**
     * Test de la méthode GET avec un serveur enregistré sur la plate-forme Flopbox mais avec un mauvais mot de passe
     */
    @Test
    @Order(2)
    public void testGetAnyServerWithFalsePassword() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/server/" + USERNAME);

        httpGet.addHeader("password", "falsePassword");

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 401);
        Assertions.assertEquals(responseMessage, "Authentification incorrect!");

        client.close();
    }

    /**
     * Test de la méthode POST pour ajouter un nouveau serveur
     *
     */
    @Test
    @Order(3)
    public void testAddServer() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/server/" + USERNAME + "?aliasName=alias");

        httpPost.addHeader("password", PASSWORD);
        httpPost.addHeader("address", "addressTest");
        httpPost.addHeader("port", "21");

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assertions.assertEquals(responseMessage, "Nouveau serveur ajouté sous l'alias : alias");

        client.close();
    }

    /**
     * Test de la méthode POST pour ajouter un nouveau serveur déjà existant
     *
     */
    @Test
    @Order(4)
    public void testAddExistingServer() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/server/" + USERNAME + "?aliasName=alias");

        httpPost.addHeader("password", PASSWORD);
        httpPost.addHeader("address", "addressTest");
        httpPost.addHeader("port", "21");

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Alias déjà existant!");

        client.close();
    }

    /**
     * Test de la méthode GET avec un serveur enregistré sur la plate-forme Flopbox
     */
    @Test
    @Order(5)
    public void testGetServer() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/server/" + USERNAME);

        httpGet.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assertions.assertEquals(responseMessage, "alias : FTPServer{address='addressTest', port=21}\n");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer l'URI d'un serveur
     *
     */
    @Test
    @Order(6)
    public void testChangeURI() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=alias&newUri=newURI");

        httpPut.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assertions.assertEquals(responseMessage, "URI modifié!");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer l'Alias d'un serveur par un Alias déjà existant
     *
     */
    @Test
    @Order(7)
    public void testChangeAliasByExistingAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=alias&newAlias=alias");

        httpPut.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Nouveau alias déjà existant!");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer l'Alias d'un serveur
     *
     */
    @Test
    @Order(8)
    public void testChangeAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=alias&newAlias=newAlias");

        httpPut.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assertions.assertEquals(responseMessage, "Alias modifié!");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer un URI avec un alias inexistant
     *
     */
    @Test
    @Order(9)
    public void testChangeURIFalseAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=falsealias&newUri=newURI");

        httpPut.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Alias non existant!");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer un alias inexistant
     *
     */
    @Test
    @Order(10)
    public void testChangeAliasFalseAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=falsealias&newAlias=newAlias");

        httpPut.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Alias non existant!");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un alias
     *
     */
    @Test
    @Order(11)
    public void testDeleteAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=newAlias");

        httpDelete.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assertions.assertEquals(responseMessage, "Alias supprimé!");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un alias inexistant
     *
     */
    @Test
    @Order(12)
    public void testDeleteFalseAlias() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/server/" + USERNAME + "?alias=falsealias");

        httpDelete.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 409);
        Assertions.assertEquals(responseMessage, "Alias non existant!");

        client.close();
    }

    /**
     * Méthode pour ajouter un compte à la plate-forme Flopbox
     *
     */
    private static void addAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/account");

        httpPost.addHeader("username", USERNAME);
        httpPost.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        client.close();
    }

    /**
     * Méthode pour supprimer un compte de la plate-forme Flopbox
     *
     */
    public static void deleteAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/account");

        httpDelete.addHeader("username", USERNAME);
        httpDelete.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        client.close();
    }
}
