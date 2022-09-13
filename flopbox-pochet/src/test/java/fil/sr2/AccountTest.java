package fil.sr2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


/**
 * Cette classe permet de tester la classe Account
 *
 * @author pochet
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountTest {

    private static HttpServer server;
    private static WebTarget target;

    /**
     * Cette méthode permet de créer et de démarrer le serveur
     *
     */
    @BeforeAll
    static public void setUp() {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        target = c.target(Main.BASE_URI);
    }

    /**
     * Cette méthode permet de stopper le serveur
     *
     */
    @AfterAll
    static public void tearDown() {
        server.stop();
    }

    /**
     * Test de la méthode GET sans aucun compte enregistré sur la plate-forme Flopbox
     */
    @Test
    @Order(1)
    public void testGetAnyAccount() {
        Response response = target.path("account").request().get();

        assertEquals(response.getStatus(), 409);
        assertEquals(response.readEntity(String.class), "Aucun utilisateur ajouter!");
    }

    /**
     * Test de la méthode POST pour ajouter un nouvel utilisateur sur la plate-forme Flopbox
     *
     */
    @Test
    @Order(2)
    public void testAddAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/account");

        httpPost.addHeader("username", "userTest");
        httpPost.addHeader("password", "passwordTest");

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Nouveau compte utilisateur ajouté sous le nom : userTest");

        client.close();
    }

    /**
     * Test de la méthode POST pour ajouter un utilisateur existant sur la plate-forme Flopbox
     *
     */
    @Test
    @Order(3)
    public void testAddExistingAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/account");

        httpPost.addHeader("username", "userTest");
        httpPost.addHeader("password", "passwordTest");

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 409);
        assertEquals(responseMessage, "Compte utilisateur déjà existant!");

        client.close();
    }

    /**
     * Test de la méthode GET avec un compte enregistré sur la plate-forme Flopbox
     */
    @Test
    @Order(4)
    public void testGetAccount() {
        Response response = target.path("account").request().get();

        assertEquals(response.getStatus(), 200);
        assertEquals(response.readEntity(String.class), "userTest : passwordTest" + "\n");
    }

    /**
     * Test de la méthode PUT pour changer le mot de passe d'un compte n'existant pas
     *
     */
    @Test
    @Order(5)
    public void testChangePasswordWithFalseAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/account");

        httpPut.addHeader("username", "falseuserTest");
        httpPut.addHeader("password", "passwordTest");
        httpPut.addHeader("newpassword", "newpasswordTest");

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 409);
        assertEquals(responseMessage, "Compte non existant!");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer le mot de passe d'un compte existant mais, avec le mauvais mot de passe
     *
     */
    @Test
    @Order(6)
    public void testChangePasswordWithFalsePassword() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/account");

        httpPut.addHeader("username", "userTest");
        httpPut.addHeader("password", "falsePasswordTest");
        httpPut.addHeader("newpassword", "newpasswordTest");

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 409);
        assertEquals(responseMessage, "Mauvais mot de passe pour le compte : userTest");

        client.close();
    }

    /**
     * Test de la méthode PUT pour changer le mot de passe d'un compte existant et avec le bon mot de passe
     *
     */
    @Test
    @Order(7)
    public void testChangePassword() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/account");

        httpPut.addHeader("username", "userTest");
        httpPut.addHeader("password", "passwordTest");
        httpPut.addHeader("newpassword", "newpasswordTest");

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Mot de passe changé!");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un compte existant, mais avec le mauvais mot de passe
     *
     */
    @Test
    @Order(8)
    public void testDeleteWithFalsePassword() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/account");

        httpDelete.addHeader("username", "userTest");
        httpDelete.addHeader("password", "falsePasswordTest");

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 409);
        assertEquals(responseMessage, "Mauvais mot de passe pour le compte : userTest");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un compte existant
     *
     */
    @Test
    @Order(9)
    public void testDeleteAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/account");

        httpDelete.addHeader("username", "userTest");
        httpDelete.addHeader("password", "newpasswordTest");

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Compte supprimé!");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un compte inexistant
     *
     */
    @Test
    @Order(10)
    public void testDeleteFalseAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/account");

        httpDelete.addHeader("username", "falseUserTest");
        httpDelete.addHeader("password", "falseNewpasswordTest");

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 409);
        assertEquals(responseMessage, "Compte non existant!");

        client.close();
    }


}
