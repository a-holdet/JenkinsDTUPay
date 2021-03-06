package messagequeue;

/***
 * @Author Benjamin Wrist Lam, s153486
 */

import accountservice.CustomerDoesNotExistException;
import accountservice.MerchantDoesNotExistException;
import messaging.rmq.event.interfaces.IEventReceiver;
import messaging.rmq.event.interfaces.IEventSender;
import messaging.rmq.event.objects.Event;
import messaging.rmq.event.objects.EventType;

import reportservice.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MessageQueueConnector implements IEventReceiver {

    private final IReportService reportService;
    IEventSender sender;

    public MessageQueueConnector(IEventSender sender, IReportService reportService) {
        this.sender = sender;
        this.reportService = reportService;
    }

    private final EventType generateReportForCustomer = new EventType("generateReportForCustomer");
    private final EventType generateReportForMerchant = new EventType("generateReportForMerchant");
    private final EventType generateManagerOverview = new EventType("generateManagerOverview");
    private final EventType registerTransaction = new EventType("registerTransaction");

    @Override
    public EventType[] getSupportedEventTypes() {
        return new EventType[]{generateReportForCustomer, generateReportForMerchant, generateManagerOverview, registerTransaction};
    }

    private void registerTransaction(Payment payment, String CustomerId) {
        reportService.registerTransaction(payment, CustomerId);
    }

	private void generateReportForCustomer(String customerId, String startTimeString, String endTimeString, UUID eventId)  {
		LocalDateTime startTime = getDateTimeFromString(startTimeString);
		LocalDateTime endTime = getDateTimeFromString(endTimeString);
		try {
			UserReport userReport = reportService.generateReportForCustomer(customerId, startTime, endTime);
			sendReportEvent(generateReportForCustomer.succeeded(),userReport,eventId);
		}
		catch (CustomerDoesNotExistException e) {
			this.sender.sendEvent(Event.GetFailedEvent(generateReportForCustomer, e, eventId));
		}
	}

    private LocalDateTime getDateTimeFromString(String datetimeString) {
        return datetimeString != null ? LocalDateTime.parse(datetimeString) : null;
    }

    private void generateReportForMerchant(String merchantId, String startTimeString, String endTimeString, UUID eventId) {
        LocalDateTime startTime = getDateTimeFromString(startTimeString);
        LocalDateTime endTime = getDateTimeFromString(endTimeString);
        try {
            UserReport userReport = reportService.generateReportForMerchant(merchantId, startTime, endTime);
			sendReportEvent(generateReportForMerchant.succeeded(),userReport,eventId);
        }
        catch (MerchantDoesNotExistException e) {
			this.sender.sendEvent(Event.GetFailedEvent(generateReportForMerchant, e, eventId));
		}
	}

    private void generateManagerOverview(UUID eventId) {
        List<Transaction> allTransactions = reportService.generateManagerOverview();
        Event event = new Event(generateManagerOverview.succeeded(), new Object[]{allTransactions}, eventId);
        try {
            this.sender.sendEvent(event);
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    private void sendReportEvent(String subject, UserReport userReport, UUID eventId) {
        Event event = new Event(subject, new Object[]{userReport}, eventId);
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

        if (type.equals(generateReportForCustomer.getName())) {
            ReportCreationDTO reportCreationDTO = event.getArgument(0, ReportCreationDTO.class);
            new Thread(() -> generateReportForCustomer(reportCreationDTO.getUserId(), reportCreationDTO.getStartTime(), reportCreationDTO.getEndTime(), eventId)).start();
        } else if (type.equals(generateReportForMerchant.getName())) {
            ReportCreationDTO reportCreationDTO = event.getArgument(0, ReportCreationDTO.class);
            new Thread(() -> generateReportForMerchant(reportCreationDTO.getUserId(), reportCreationDTO.getStartTime(), reportCreationDTO.getEndTime(), eventId)).start();
        } else if (type.equals(generateManagerOverview.getName())) {
            generateManagerOverview(eventId);
        } else if (type.equals(registerTransaction.getName())) {
            Payment payment = event.getArgument(0, Payment.class);
            String customerId = event.getArgument(1, String.class);
            registerTransaction(payment, customerId);
        }

        System.out.println("--------------------------------------------------------");
    }


}