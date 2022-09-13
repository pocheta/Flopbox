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
public interface AccountApi  {

    /**
     * Creates new account
     *
     */
    @POST
    @Path("/account")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Creates new account", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Account created successfuly"),
        @ApiResponse(code = 409, message = "Account already exists") })
    public void createAccount(@HeaderParam("username") String username, @HeaderParam("password") String password);

    /**
     * Deletes account
     *
     */
    @DELETE
    @Path("/account")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Deletes account", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Account deleted successfuly"),
        @ApiResponse(code = 409, message = "Account doesn't exists or bad current password") })
    public void deleteAccount(@HeaderParam("username") String username, @HeaderParam("password") String password);

    /**
     * Retrieves all accounts
     *
     */
    @GET
    @Path("/account")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Retrieves all accounts", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Accounts retrieved successfuly"),
        @ApiResponse(code = 409, message = "No account found") })
    public void getAccounts();

    /**
     * Modifies password
     *
     */
    @PUT
    @Path("/account")
    @Produces({ "text/plain" })
    @ApiOperation(value = "Modifies password", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Account's password updated successfuly"),
        @ApiResponse(code = 409, message = "Account doesn't exists or bad current password") })
    public void updatePasswordAccount(@HeaderParam("username") String username, @HeaderParam("password") String password, @HeaderParam("newpassword") String newpassword);
}

