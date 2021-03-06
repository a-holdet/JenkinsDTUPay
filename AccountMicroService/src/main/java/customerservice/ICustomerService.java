package customerservice;

public interface ICustomerService {
    String registerCustomer(Customer customer) throws IllegalArgumentException;

    boolean customerExists(String customerId);

    Customer getCustomer(String customerId) throws CustomerDoesNotExistException;
}
