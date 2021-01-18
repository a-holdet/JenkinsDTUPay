package paymentservice;

import DTO.Payment;
import accountservice.customerservice.CustomerDoesNotExistException;
import accountservice.merchantservice.MerchantDoesNotExistException;
import messaging.rmq.event.objects.EventServiceBase;
import messaging.rmq.event.interfaces.IEventSender;
import messaging.rmq.event.objects.Event;
import messaging.rmq.event.objects.EventType;
import Bank.BankPortException;
import tokenservice.ConsumeTokenException;
import tokenservice.TokenDoesNotExistException;

public class MessageQueuePaymentService extends EventServiceBase implements IPaymentService {

    private static final EventType registerPayment = new EventType("registerPayment");
    private static final EventType[] supportedEventTypes = new EventType[] {registerPayment};

    public MessageQueuePaymentService(IEventSender sender) {
        super(sender, supportedEventTypes);
    }

    @Override
    public void registerPayment(Payment payment) throws TokenDoesNotExistException, MerchantDoesNotExistException, NegativeAmountException, CustomerDoesNotExistException, ConsumeTokenException, BankPortException {
        System.out.println("REGISTER PAYMENT: ");
        Event response = sendRequestAndAwaitReponse(payment, registerPayment);

        System.out.println("REGISTER PAYMENT2: ");
        if (response.isFailureReponse()) {
            String exceptionType = response.getArgument(0, String.class); //TODO: Refactor into own method on "response"
            String exceptionMessage = response.getErrorMessage();
            switch (exceptionType) {
                case "MerchantDoesNotExistException": throw new MerchantDoesNotExistException(exceptionMessage);
                case "NegativeAmountException": throw new NegativeAmountException(exceptionMessage);
                case "BankException": throw new BankPortException(exceptionMessage);
                case "ConsumeTokenException": throw new ConsumeTokenException(exceptionMessage);
                case "TokenDoesNotExistException": throw new TokenDoesNotExistException(exceptionMessage);
                case "CustomerDoesNotExistException": throw new CustomerDoesNotExistException(exceptionMessage);
            }
        }
    }
}