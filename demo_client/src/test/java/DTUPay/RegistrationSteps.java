package DTUPay;

import CustomerMobileApp.UserManagementAdapter;
import dtu.ws.fastmoney.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class RegistrationSteps {
    User customer;
    String customerId;
    String errorMessage;
    UserManagementAdapter userManagementAdapter = new UserManagementAdapter();


    @Given("the customer {string} {string} with CPR {string} does not have a bank account")
    public void theCustomerWithCPRDoesNotHaveABankAccount(String firstName, String lastName, String cprNumber) {
        customer = new User();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCprNumber(cprNumber);
    }

    @When("the customer is registering with DTUPay")
    public void theCustomerIsRegisteringWithDTUPay() {
        try {
            customerId = userManagementAdapter.registerCustomer(customer.getFirstName(), customer.getLastName(), customer.getCprNumber(),null);
        } catch (IllegalArgumentException e) {
            customerId = null;
            errorMessage = e.getMessage();
        }
    }

    @Then("the registration is not successful")
    public void theRegistrationIsNotSuccessful() {
        assertNull(customerId);
    }

    @And("the error message is {string}")
    public void theErrorMessageIs(String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, errorMessage);
    }
}
