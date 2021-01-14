package CustomerMobileApp;

import CustomerMobileApp.DTO.DTUPayUser;
import CustomerMobileApp.DTO.TokenRequestObject;
import io.quarkus.security.UnauthorizedException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class CustomerAdapter {
    WebTarget baseUrl;

    public CustomerAdapter(){
        Client client = ClientBuilder.newClient();
        baseUrl = client.target("http://localhost:8042/customerapi");
    }

    public String registerCustomer(String firstName, String lastName, String cprNumber, String accountID) throws IllegalArgumentException {
        DTUPayUser customer = new DTUPayUser(firstName, lastName, cprNumber, accountID);
        Response response = baseUrl.path("customers").request().post(Entity.entity(customer, MediaType.APPLICATION_JSON));

        if(response.getStatus()==422){
            String errorMessage = response.readEntity(String.class); //error message is in payload
            response.close();
            throw new IllegalArgumentException(errorMessage);
        }

        //TODO: unsure what is returned by server here.
        // Assumes it to be accountId.
        String accountId = response.readEntity(String.class);
        response.close();
        return accountId;
    }

    public List<UUID> createTokensForCustomer(String customerId, int amount) throws Exception {
        TokenRequestObject request = new TokenRequestObject();
        request.setUserId(customerId);
        request.setTokenAmount(amount);
        Response response = baseUrl
                .path("tokens")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        if (response.getStatus() == 401) { // customer is unauthorized (i.e customer has no bank account)
            String errorMessage = response.readEntity(String.class); // error message is in payload
            response.close();
            throw new UnauthorizedException(errorMessage);
        } else if (response.getStatus() == 403) { // Customer not allowed to request more tokens
            String errorMessage = response.readEntity(String.class); // error message is in payload
            response.close();
            throw new Exception(errorMessage);
        }

        List<UUID> createdTokens = response.readEntity(new GenericType<List<UUID>>() {});
        response.close();
        return createdTokens;
    }

}
