package io.swagger.api;


import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import fil.sr2.object.ArrayOf;
import org.apache.cxf.jaxrs.ext.multipart.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import java.io.File;
import java.io.InputStream;

/**
 * Swagger FlopBox
 *
 * <p>This is the FlopBox swagger.
 */
@Path("/")
@Api(value = "/", description = "")
public interface ClientApi {

    /**
     * Upload a file/folder to a FTP server
     */
    @POST
    @Path("/client/{flopBoxUsername}")
    @Consumes({"multipart/form-data"})
    @Produces({"text/plain"})
    @ApiOperation(value = "Creates a folder or upload a file/folder to a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Folder created | file/folder uploaded successfuly"),
            @ApiResponse(code = 400, message = "Unable to create folder | Unable to send file/folder"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists")})
    public Response addToFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("path") String path, @Multipart("file") File file);

    /**
     * Creates a folder to a FTP server
     */
    @POST
    @Path("/client/{flopBoxUsername}")
    @Consumes({"multipart/form-data"})
    @Produces({"text/plain"})
    @ApiOperation(value = "Creates a folder or upload a file/folder to a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Folder created | file/folder uploaded successfuly"),
            @ApiResponse(code = 400, message = "Unable to create folder | Unable to send file/folder"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists")})
    public Response addFolderToFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("path") String path, @QueryParam("directoryName") String directoryName);


    /**
     * Lists files of a FTP server
     */
    @GET
    @Path("/client/{flopBoxUsername}")
    @Produces({"application/json"})
    @ApiOperation(value = "Lists files of a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Listed successfuly"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists")})
    public ArrayOf listFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("path") String path);

    /**
     * Removes file/folder of a FTP server
     */
    @DELETE
    @Path("/client/{flopBoxUsername}")
    @Produces({"text/plain"})
    @ApiOperation(value = "Removes file/folder of a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Removed successfuly"),
            @ApiResponse(code = 400, message = "Unable to remove file/folder"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists")})
    public Response removeFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("path") String path);

    /**
     * Renames file/folder of a FTP server
     */
    @PUT
    @Path("/client/{flopBoxUsername}")
    @Produces({"text/plain"})
    @ApiOperation(value = "Renames file/folder of a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Renamed | mode changed successfuly"),
            @ApiResponse(code = 400, message = "Unable to rename file/folder | incorrect param"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists"),
            @ApiResponse(code = 500, message = "File/folder doesn't exists")})
    public Response renameFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("path") String path, @QueryParam("newName") String newName, @QueryParam("mode") String mode);

    /**
     * Retrieves file/folder of a FTP server
     */
    @GET
    @Path("/client/{flopBoxUsername}/download")
    @Produces({"application/json", "application/octet-stream"})
    @ApiOperation(value = "Retrieves file/folder of a FTP server", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "File/folder retrieved successfuly"),
            @ApiResponse(code = 400, message = "File doesn't exists"),
            @ApiResponse(code = 401, message = "FlopBox bad authentication"),
            @ApiResponse(code = 409, message = "Param error | Alias doesn't exists")})
    public InputStream retrieveFTPServer(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("flopBoxPassword") String flopBoxPassword, @PathParam("flopBoxUsername") String flopBoxUsername, @QueryParam("alias") String alias, @QueryParam("filePath") String filePath);
}

