package edu.stanford.protege.versioning.services.email;

import com.mailgun.api.v3.*;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.*;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
public class MailgunApiService {

    private static final Logger log = LoggerFactory.getLogger(MailgunApiService.class);

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.mailgun.base-url}")
    private String baseUrl;

    @Value("${mail.mailgun.api-key}")
    private String apiKey;

    @Value("${mail.mailgun.domain}")
    private String domain;

    @Value("${mail.mailgun.send-emails}")
    private Boolean sendEmails;

    @Value("${mail.destination-override}")
    private String destinationOverride;

    @Value("${mail.notification-mass-target}")
    private String notificationMassTarget;

    private MailgunMessagesApi messagesApi;
    private MailgunMailingListApi mailingListApi;

    @PostConstruct
    public void init() {
        MailgunClient.MailgunClientBuilder mailgunClientBuilder = MailgunClient.config(baseUrl, apiKey);
        messagesApi = mailgunClientBuilder.createApi(MailgunMessagesApi.class);
        mailingListApi = mailgunClientBuilder.createApi(MailgunMailingListApi.class);

        log.info("Send email active:{} bcc: {} with: {} {}", sendEmails, destinationOverride, messagesApi, mailingListApi);
    }

    public void sendMail(Exception exception) {
        try {
            String to = StringUtils.isNotBlank(destinationOverride) ? destinationOverride : "default@example.com";

            String currentUtc = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            String subject = "Backup Exception Occurred";
            String htmlContent = String.format("An exception has occured when trying to make backup for %s:<br><br>"
                            + "<b>Exception Message:</b> %s<br><br>"
                            + "<b>Stack Trace:</b><pre>%s</pre>",
                    currentUtc,
                    exception.getMessage(),
                    stackTrace);

            Message message = Message.builder()
                    .from(fromName + "<" + fromEmail + ">")
                    .subject(subject)
                    .html(htmlContent)
                    .to(to)
                    .build();

            if (sendEmails) {
                MessageResponse response = messagesApi.sendMessage(domain, message);
                log.info("Exception email sent to: {} response: {}", to, response);
            } else {
                log.warn("Email sending disabled, attempted to send exception email to: {}", to);
            }
        } catch (Exception e) {
            log.error("Error while sending mail ", e);
        }

    }
}
