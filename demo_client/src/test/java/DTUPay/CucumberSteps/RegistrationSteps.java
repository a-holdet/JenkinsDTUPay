package DTUPay.CucumberSteps;

import CustomerMobileApp.UserManagementAdapter;
import DTUPay.Holders.*;
import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

public class RegistrationSteps {
    //Adapters
    UserManagementAdapter userManagementAdapter = new UserManagementAdapter();
    BankService bankService = new BankServiceService().getBankServicePort();

    //Holders
    private final CustomerHolder customerHolder;
    private final MerchantHolder merchantHolder;
    private final OtherMerchantHolder otherMerchantHolder;
    ExceptionHolder exceptionHolder;

    public RegistrationSteps(CustomerHolder customerHolder, MerchantHolder merchantHolder, OtherMerchantHolder otherMerchantHolder, ExceptionHolder exceptionHolder) {
        this.customerHolder = customerHolder;
        this.merchantHolder = merchantHolder;
        this.otherMerchantHolder = otherMerchantHolder;
        this.exceptionHolder = exceptionHolder;
    }

    @After
    public void after(){
        try {
            if (customerHolder.getAccountId() != null)
                bankService.retireAccount(customerHolder.getAccountId());
        } catch (BankServiceException_Exception e) {
        }
        try {
            if (merchantHolder.getAccountId() != null)
                bankService.retireAccount(merchantHolder.getAccountId());
        } catch (BankServiceException_Exception e) {
        }

        //TODO should we retire DTUPay account?
        customerHolder.reset();
        merchantHolder.reset();
        otherMerchantHolder.reset();
    }

    @And("the customer is registered with DTUPay")
    public void theCustomerIsRegisteredWithDTUPay() {
        try {
            customerHolder.setId(userManagementAdapter.registerCustomer(customerHolder.getFirstName(), customerHolder.getLastName(), customerHolder.getCpr(), customerHolder.getAccountId()));
            assertNotNull(customerHolder.getId());
        } catch (IllegalArgumentException e){
            exceptionHolder.setException(e);
            customerHolder.setId(null);
        }
    }

    private int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private void setCustomerHolderBasics(){
        int lastFour = getRandomNumberInRange(1000,9999);
        String cpr = "200167-"+ lastFour;

        customerHolder.setFirstName("Stein");
        customerHolder.setLastName("Bagger");
        customerHolder.setCpr(cpr);
    }

    private void setMerchantHolderBasicsFor(UserHolder merchantHolder){
        int lastFour = getRandomNumberInRange(1000,9999);
        String cpr = "150363-"+ lastFour;


        merchantHolder.setFirstName("Joe");
        merchantHolder.setLastName("Exotic");
        merchantHolder.setCpr(cpr);
    }

    @Given("the customer has a bank account")
    public void theCustomerHasABankAccount() throws Exception {
        setCustomerHolderBasics();
        User customerBank = new User();
        customerBank.setFirstName(customerHolder.getFirstName());
        customerBank.setLastName(customerHolder.getLastName());
        customerBank.setCprNumber(customerHolder.getCpr());

        customerBank.setFirstName(customerHolder.getFirstName());
        customerBank.setLastName(customerHolder.getLastName());
        customerBank.setCprNumber(customerHolder.getCpr());
        customerHolder.setAccountId(bankService.createAccountWithBalance(customerBank, new BigDecimal(1000)));
    }

    @And("the merchant has a bank account")
    public void theMerchantHasABankAccount() {
        setMerchantHolderBasicsFor(merchantHolder);
        User merchantBank = new User();
        merchantBank.setFirstName(merchantHolder.getFirstName());
        merchantBank.setLastName(merchantHolder.getLastName());
        merchantBank.setCprNumber(merchantHolder.getCpr());
        try {
            merchantHolder.setAccountId(bankService.createAccountWithBalance(merchantBank, new BigDecimal(2000)));
        } catch (BankServiceException_Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @And("the merchant is registered with DTUPay")
    public void theMerchantIsRegisteredWithDTUPay() {
        registerMerchantWithDTUPay(merchantHolder);
    }

    @And("the merchant is not registered with DTUPay")
    public void theMerchantIsNotRegisteredWithDTUPay() {
        //Do not register merchant with DTUPay
    }

    @Then("the registration is not successful")
    public void theRegistrationIsNotSuccessful() {
        assertNull(customerHolder.getId());
    }

    @And("the error message is {string}")
    public void theErrorMessageIs(String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, exceptionHolder.getException().getMessage());
    }

    @Given("the customer has no bank account")
    public void theCustomerWithNameAndCPRHasNoBankAccount() {
        setCustomerHolderBasics();
        //Do not create account
    }


    @And("another merchant has a bank account")
    public void anotherMerchantHasABankAccount() {
        setMerchantHolderBasicsFor(otherMerchantHolder);
        User merchantBank = new User();
        merchantBank.setFirstName(otherMerchantHolder.getFirstName());
        merchantBank.setLastName(otherMerchantHolder.getLastName());
        merchantBank.setCprNumber(otherMerchantHolder.getCpr());
        try {
            otherMerchantHolder.setAccountId(bankService.createAccountWithBalance(merchantBank, new BigDecimal(2000)));
        } catch (BankServiceException_Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @And("the other merchant is registered with DTUPay")
    public void theOtherMerchantIsRegisteredWithDTUPay() {
        registerMerchantWithDTUPay(otherMerchantHolder);
    }

    private void registerMerchantWithDTUPay(UserHolder merchantHolder) {
        merchantHolder.setId(userManagementAdapter.registerMerchant(merchantHolder.getFirstName(), merchantHolder.getLastName(), merchantHolder.getCpr(), merchantHolder.getAccountId()));
    }
}
