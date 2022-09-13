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
 * API tests for ServerApi 
 */
public class ServerApiTest {


    private ServerApi api;
    
    @Before
    public void setup() {
        JacksonJsonProvider provider = new JacksonJsonProvider();
        List providers = new ArrayList();
        providers.add(provider);
        
        api = JAXRSClientFactory.create("http://localhost:8080/flopbox", ServerApi.class, providers);
        org.apache.cxf.jaxrs.client.Client client = WebClient.client(api);
        
        ClientConfiguration config = WebClient.getConfig(client); 
    }

    
    /**
     * Creates new FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void createFTPServerTest() {
        String password = null;
        String address = null;
        Integer port = null;
        String username = null;
        String aliasName = null;
        //api.createFTPServer(password, address, port, username, aliasName);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Deletes FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void deleteFTPServerTest() {
        String password = null;
        String username = null;
        String alias = null;
        //api.deleteFTPServer(password, username, alias);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Retrieves all FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getFTPServersTest() {
        String password = null;
        String username = null;
        //api.getFTPServers(password, username);
        
        // TODO: test validations
        
        
    }
    
    /**
     * Updates FTP server
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void updateFTPServerTest() {
        String password = null;
        String username = null;
        String alias = null;
        String newAlias = null;
        String newUri = null;
        //api.updateFTPServer(password, username, alias, newAlias, newUri);
        
        // TODO: test validations
        
        
    }
    
}
