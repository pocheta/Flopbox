/**
 * Swagger FlopBox
 * This is the FlopBox swagger.
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.swagger.api;

import java.io.File;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;


import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




/**
 * Swagger FlopBox
 *
 * <p>This is the FlopBox swagger.
 *
 * API tests for ClientApi 
 */
public class ClientApiTest {


    private ClientApi api;
    
    @Before
    public void setup() {
        JacksonJsonProvider provider = new JacksonJsonProvider();
        List providers = new ArrayList();
        providers.add(provider);
        
        api = JAXRSClientFactory.create("http://localhost:8080/flopbox", ClientApi.class, providers);
        org.apache.cxf.jaxrs.client.Client client = WebClient.client(api);
        
        ClientConfiguration config = WebClient.getConfig(client); 
    }

    
    /**
     * Creates a folder or upload a file/folder to a FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void addToFTPServerTest() {
        String username = null;
        String password = null;
        String flopBoxPassword = null;
        String flopBoxUsername = null;
        String alias = null;
        String path = null;
        String directoryName = null;
        org.apache.cxf.jaxrs.ext.multipart.Attachment file = null;
        //api.addToFTPServer(username, password, flopBoxPassword, flopBoxUsername, alias, path, directoryName, file);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Lists files of a FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listFTPServerTest() {
        String username = null;
        String password = null;
        String flopBoxPassword = null;
        String flopBoxUsername = null;
        String alias = null;
        String path = null;
        //api.listFTPServer(username, password, flopBoxPassword, flopBoxUsername, alias, path);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Removes file/folder of a FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void removeFTPServerTest() {
        String username = null;
        String password = null;
        String flopBoxPassword = null;
        String flopBoxUsername = null;
        String alias = null;
        String path = null;
        //api.removeFTPServer(username, password, flopBoxPassword, flopBoxUsername, alias, path);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Renames file/folder of a FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void renameFTPServerTest() {
        String username = null;
        String password = null;
        String flopBoxPassword = null;
        String flopBoxUsername = null;
        String alias = null;
        String path = null;
        String newName = null;
        String mode = null;
        //api.renameFTPServer(username, password, flopBoxPassword, flopBoxUsername, alias, path, newName, mode);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Retrieves file/folder of a FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void retrieveFTPServerTest() {
        String username = null;
        String password = null;
        String flopBoxPassword = null;
        String flopBoxUsername = null;
        String alias = null;
        String filePath = null;
        //api.retrieveFTPServer(username, password, flopBoxPassword, flopBoxUsername, alias, filePath);
        
        // TODO: test validations
        
        
    }
    
}
