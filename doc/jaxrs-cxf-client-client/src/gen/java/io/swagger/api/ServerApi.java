package io.swagger.api;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.ext.multipart.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.jaxrs.PATCH;

/**
 * Swagger FlopBox
 *
 * <p>This is the FlopBox swagger.
 *
 */
@Path("/")
@Api(value = "/", description = "")
public interface ServerApi  {

    /**
     * Creates new FTP server
     *
     */
    @POST
    @Path("/server/{username}")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Creates new FTP server", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "FTP server created successfuly"),
        @ApiResponse(code = 401, message = "FlopBox bad authentication"),
        @ApiResponse(code = 409, message = "FTP server already exists") })
    public void createFTPServer(@HeaderParam("password") String password, @HeaderParam("address") String address, @HeaderParam("port") Integer port, @PathParam("username") String username, @QueryParam("aliasName")String aliasName);

    /**
     * Deletes FTP server
     *
     */
    @DELETE
    @Path("/server/{username}")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Deletes FTP server", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "FTP server updated successfuly"),
        @ApiResponse(code = 401, message = "FlopBox bad authentication"),
        @ApiResponse(code = 409, message = "Param error | Alias doesn't exists") })
    public void deleteFTPServer(@HeaderParam("password") String password, @PathParam("username") String username, @QueryParam("alias")String alias);

    /**
     * Retrieves all FTP server
     *
     */
    @GET
    @Path("/server/{username}")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Retrieves all FTP server", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "FTP servers retrieved successfuly"),
        @ApiResponse(code = 401, message = "FlopBox bad authentication"),
        @ApiResponse(code = 409, message = "No FTP server found") })
    public void getFTPServers(@HeaderParam("password") String password, @PathParam("username") String username);

    /**
     * Updates FTP server
     *
     */
    @PUT
    @Path("/server/{username}")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Updates FTP server", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "FTP server updated successfuly"),
        @ApiResponse(code = 401, message = "FlopBox bad authentication"),
        @ApiResponse(code = 409, message = "Param error | Alias doesn't exists") })
    public void updateFTPServer(@HeaderParam("password") String password, @PathParam("username") String username, @QueryParam("alias")String alias, @QueryParam("newAlias")String newAlias, @QueryParam("newUri")String newUri);
}

