package fil.sr2;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Cette classe permet de tester la classe Server
 *
 * @author pochet
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientTest {

    private HttpServer server;
    private FakeFtpServer fakeFtpServer;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPassword";

    /**
     * Cette méthode permet de créer et de démarrer le serveur et de créer un faux serveur FTP pour éffectuer les tests
     *
     */
    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        WebTarget target = c.target(Main.BASE_URI);

        launchLocalFTPServer();
        addClientAndServer();
    }

    /**
     * Cette méthode permet de stopper le serveur
     *
     */
    @After
    public void tearDown() throws Exception {
        fakeFtpServer.stop();
        deleteAccount();
        server.stop();
    }

    /**
     * Test de la méthode GET pour récupérer les fichiers présent sur le serveur FTP
     *
     */
    @Test
    @Order(1)
    public void testListFTP() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=");

        httpGet.addHeader("flopboxPassword", PASSWORD);
        httpGet.addHeader("username", "anonymous");
        httpGet.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "[\"directory\\\\true\\\\0\\\\2022-04-08\\\\00:00:00\",\"test.txt\\\\false\\\\34\\\\2022-04-08\\\\00:00:00\"]");

        client.close();
    }

    /**
     * Test de la méthode GET pour récupérer les fichiers présent sur le serveur FTP avec chemin inexistant sur le serveur FTP
     *
     */
    @Test
    @Order(2)
    public void testEmptyListFTP() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=falsedirectory");

        httpGet.addHeader("flopboxPassword", PASSWORD);
        httpGet.addHeader("username", "anonymous");
        httpGet.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "[]");

        client.close();
    }

    /**
     * Test de la méthode GET pour télécharger un fichier présent sur le serveur FTP
     *
     */
    @Test
    @Order(3)
    public void testDownloadFile() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/client/" + USERNAME + "/download?alias=alias&filePath=/home/test.txt");

        httpGet.addHeader("flopboxPassword", PASSWORD);
        httpGet.addHeader("username", "anonymous");
        httpGet.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "azertyuiopqsdfghjklmwxcvb123456789");

        client.close();
    }

    /**
     * Test de la méthode GET pour télécharger un dossier présent sur le serveur FTP
     *
     */
    @Test
    @Order(4)
    public void testDownloadDirectory() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/flopbox/client/" + USERNAME + "/download?alias=alias&directoryPath=/home");

        httpGet.addHeader("flopboxPassword", PASSWORD);
        httpGet.addHeader("username", "anonymous");
        httpGet.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpGet);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "directory/\n" +
                "\ttest2.txt\n" +
                "\ttest.txt\n");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un fichier présent sur le serveur FTP
     *
     */
    @Test
    @Order(5)
    public void testDeleteFile() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=/home/test.txt");

        httpDelete.addHeader("flopboxPassword", PASSWORD);
        httpDelete.addHeader("username", "anonymous");
        httpDelete.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Fichier supprimer\n");

        client.close();
    }

    /**
     * Test de la méthode DELETE pour supprimer un fichier inexistant sur le serveur FTP
     *
     */
    @Test
    @Order(5)
    public void testDeleteFalseFile() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=/home/falseFile.txt");

        httpDelete.addHeader("flopboxPassword", PASSWORD);
        httpDelete.addHeader("username", "anonymous");
        httpDelete.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpDelete);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 400);
        assertEquals(responseMessage, "Impossible de supprimer le fichier/dossier \n");

        client.close();
    }

    /**
     * Test de la méthode POST pour créer un nouveau répertoire sur le serveur FTP
     *
     */
    @Test
    @Order(6)
    public void testCreateDirectory() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=/home&directoryName=newDirectory");

        httpPost.addHeader("flopboxPassword", PASSWORD);
        httpPost.addHeader("username", "anonymous");
        httpPost.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Dossier créer\n");

        client.close();
    }

    /**
     * Test de la méthode PUT pour renommer un fichier sur le serveur FTP
     *
     */
    @Test
    @Order(7)
    public void testRenameFile() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:8080/flopbox/client/" + USERNAME + "?alias=alias&path=test.txt&newName=newFileName.txt");

        httpPut.addHeader("flopboxPassword", PASSWORD);
        httpPut.addHeader("username", "anonymous");
        httpPut.addHeader("password", "anonymous");

        CloseableHttpResponse response = client.execute(httpPut);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(responseMessage, "Fichier/Dossier renommer !\n");

        client.close();
    }

    /**
     * Méthode pour créer un faux serveur FTP pour effectuer les tests
     */
    public void launchLocalFTPServer() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("anonymous", "anonymous", "/home"));

        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/home"));
        fileSystem.add(new FileEntry("/home/test.txt", "azertyuiopqsdfghjklmwxcvb123456789"));
        fileSystem.add(new FileEntry("/home/directory/test2.txt", "azertyuiopqsdfghjklmwxcvb123456789"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(2121);

        fakeFtpServer.start();
    }

    /**
     * Méthode pour ajouter un compte et un serveur sur la plate-forme Flopbox
     *
     */
    public void addClientAndServer() throws IOException {
        addAccount();
        addServer();
    }

    /**
     * Méthode pour ajouter un compte à la plate-forme Flopbox
     *
     */
    private void addAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/account");

        httpPost.addHeader("username", USERNAME);
        httpPost.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpPost);

        String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

        client.close();
    }

    /**
     * Méthode pour ajouter un nouveau serveur sur la plate-forme Flopbox
     *
     */
    public void addServer() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/flopbox/server/" + USERNAME + "?aliasName=alias");

        httpPost.addHeader("password", PASSWORD);
        httpPost.addHeader("address", "localhost");
        httpPost.addHeader("port", "2121");

        CloseableHttpResponse response = client.execute(httpPost);

        client.close();
    }

    /**
     * Méthode pour supprimer un compte de la plate-forme Flopbox
     *
     */
    public void deleteAccount() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete("http://localhost:8080/flopbox/account");

        httpDelete.addHeader("username", USERNAME);
        httpDelete.addHeader("password", PASSWORD);

        CloseableHttpResponse response = client.execute(httpDelete);

        client.close();
    }


}
