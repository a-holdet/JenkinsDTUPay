package messagequeue;


import accountservice.CustomerDoesNotExistException;
import accountservice.MerchantDoesNotExistException;
import messaging.rmq.event.EventExchangeFactory;
import messaging.rmq.event.interfaces.IEventReceiver;
import messaging.rmq.event.interfaces.IEventSender;
import messaging.rmq.event.objects.Event;
import messaging.rmq.event.objects.EventType;

import reportservice.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageQueueConnector implements IEventReceiver {

	private final IReportService reportService;
	IEventSender sender;
	private ConcurrentHashMap<String, List<IEventReceiver>> eventTypeMapper = new ConcurrentHashMap<>();

	public MessageQueueConnector(IEventSender sender, IReportService reportService)
	{
		this.sender = sender;
		this.reportService=reportService;
	}

	private final EventType generateReportForCustomer = new EventType("generateReportForCustomer");
    private final EventType generateReportForMerchant = new EventType("generateReportForMerchant");
    private final EventType generateManagerOverview = new EventType("generateManagerOverview");
	private final EventType registerTransaction = new EventType("registerTransaction");

	@Override
	public EventType[] getSupportedEventTypes() {
		return new EventType[] {generateReportForCustomer, generateReportForMerchant, generateManagerOverview, registerTransaction};
	}

	private void registerTransaction(Payment payment, String CustomerId) {
		System.out.println("register transaction");
		reportService.registerTransaction(payment, CustomerId);
		System.out.println("register transaction done");
	}

	private void generateReportForCustomer(String customerId, String startTime, String endTime, UUID eventId)  {
		try {
			UserReport userReport = reportService.generateReportForCustomer(customerId, startTime, endTime);
			sendReportEvent(generateReportForCustomer.succeeded(),userReport,eventId);
		}
		catch (CustomerDoesNotExistException e) {
			String exceptionType = e.getClass().getSimpleName();
			String exceptionMsg = e.getMessage();
            Event event = new Event(generateReportForCustomer.failed(), new Object[] { exceptionType, exceptionMsg }, eventId);
			try {
				this.sender.sendEvent(event);
			} catch (Exception exception) {
				throw new Error(exception);
			}
		}
	}

    private void generateReportForMerchant(String merchantId, String startTime, String endTime, UUID eventId) {
        try {
			System.out.println("Invoking report service");
            UserReport userReport = reportService.generateReportForMerchant(merchantId, startTime, endTime);
			System.out.println("Reporting service generated report, now sending report event");
			sendReportEvent(generateReportForMerchant.succeeded(),userReport,eventId);
        }
        catch (MerchantDoesNotExistException e) {
			String exceptionType = e.getClass().getSimpleName();
			String exceptionMsg = e.getMessage();
			Event event = new Event(generateReportForMerchant.failed(), new Object[]{exceptionType, exceptionMsg}, eventId);
			System.out.println("Failed generated report for merchant - sending exception:" + exceptionType + " and msg: " + exceptionMsg);
			try {
				this.sender.sendEvent(event);
			} catch (Exception exception) {
				throw new Error(exception);
			}
		}
	}

    private void generateManagerOverview(UUID eventId){
        List<Transaction> allTransactions = reportService.generateManagerOverview();
		for(Transaction transaction : allTransactions)
			System.out.println(transaction.datetime);
        Event event = new Event(generateManagerOverview.succeeded(), new Object[] { allTransactions }, eventId);
		try {
			this.sender.sendEvent(event);
		} catch (Exception e) {
			throw new Error(e);
		}
	}


	private void sendReportEvent(String subject, UserReport userReport, UUID eventId){
		System.out.println("Sending succesfull report: " + subject);
		System.out.println("Payload is  " + userReport.getUser());
		System.out.println("Payload is  " + userReport.getPayments());
		Event event = new Event(subject, new Object[] { userReport.getUser(), userReport.getPayments() }, eventId);
		try {
			this.sender.sendEvent(event);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	// TODO: Should not use threads any longer
	@Override
	public void receiveEvent(Event event) {
		System.out.println("--------------------------------------------------------");
		System.out.println("Event received! : " + event);

		String type = event.getEventType();
		UUID eventId = event.getUUID();

        if (type.equals(generateReportForCustomer.getName())){
			System.out.println("customer overview");
            String customerId = event.getArgument(0, String.class);
            String startTime = event.getArgument(1, String.class);
            String endTime = event.getArgument(2, String.class);
			new Thread(() -> {
				generateReportForCustomer(customerId, startTime, endTime, eventId);
			}).start();
        } else if (type.equals(generateReportForMerchant.getName())){
			System.out.println("merchant overview");
            String merchantId = event.getArgument(0, String.class);
            String startTime = event.getArgument(1, String.class);
            String endTime = event.getArgument(2, String.class);
			new Thread(() -> {
				generateReportForMerchant(merchantId, startTime, endTime, eventId);
			}).start();
        } else if (type.equals(generateManagerOverview.getName())){
			System.out.println("manager overview");
            generateManagerOverview(eventId);
        } else if(type.equals(registerTransaction.getName())){
			System.out.println("Extracing payload for register transaction");
        	Payment payment = event.getArgument(0, Payment.class);
			String customerId = event.getArgument(1, String.class);
        	registerTransaction(payment,customerId);
			System.out.println("finished invoking register transaction");
		}

		System.out.println("--------------------------------------------------------");
	}


}