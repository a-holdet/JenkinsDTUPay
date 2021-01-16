package adapters;

import DTO.TokenCreationDTO;
import customerservice.*;
import messaging.rmq.event.EventExchange;
import messaging.rmq.event.EventQueue;
import messaging.rmq.event.interfaces.IEventSender;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/customerapi")
public class CustomerResource {
    ICustomerService ICustomerService = LocalCustomerService.instance;

    public CustomerResource(){
        CustomerPortAdapter.startUp();

        IEventSender sender = EventExchange.instance.getSender();
        customerPA = new CustomerPA(sender);
        try {
            new EventQueue().registerReceiver(customerPA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("customerResource constructor called");
    }

    @POST
    @Path("/customers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Customer customer) {
        try{
            String customerId = ICustomerService.registerCustomer(customer);
            return Response.ok(customerId).build();
        }catch(IllegalArgumentException e){
            return Response.status(422).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTokensForCustomer(TokenCreationDTO request) {
        String customerId = request.getUserId();
        int amount = request.getTokenAmount();
        System.out.println(customerId);
        System.out.println(amount);
        System.out.println("hey");
        List<UUID> tokens = null;
        try {
            tokens = customerPA.createTokensForCustomer(customerId, amount);
        } catch (Exception e) {
            e.printStackTrace();

            // send 500
        }
        return Response.ok(tokens).build();
    }
}
