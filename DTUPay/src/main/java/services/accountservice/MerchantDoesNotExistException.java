package services.accountservice;

public class MerchantDoesNotExistException extends Throwable {
    public MerchantDoesNotExistException(String errormsg) {
        super(errormsg);
    }
}
