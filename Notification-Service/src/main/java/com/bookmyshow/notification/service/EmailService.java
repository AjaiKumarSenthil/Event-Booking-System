package com.bookmyshow.notification.service;

import com.bookmyshow.notification.enums.BookingEventType;
import com.bookmyshow.notification.event.BookingEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter SHOW_TIME_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' hh:mm a", Locale.ENGLISH);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${notification.from-email}")
    private String fromEmail;

    @Value("${notification.from-name}")
    private String fromName;

    public void sendBookingEmail(BookingEvent event) throws MessagingException, UnsupportedEncodingException {
        String template = templateFor(event.getEventType());
        String subject = subjectFor(event);
        String htmlBody = renderTemplate(template, event);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(fromEmail, fromName);
        helper.setTo(event.getUserEmail());
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        log.info("Sending {} email to {} for booking {}",
                event.getEventType(), event.getUserEmail(), event.getBookingId());
        try {
            mailSender.send(message);
            log.info("Email sent: bookingId={} to={} type={}",
                    event.getBookingId(), event.getUserEmail(), event.getEventType());
        } catch (org.springframework.mail.MailException ex) {
            log.error("Email send failed: bookingId={} to={} type={}: {}",
                    event.getBookingId(), event.getUserEmail(),
                    event.getEventType(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private String renderTemplate(String templateName, BookingEvent event) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("userName", event.getUserName());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("movieTitle", event.getMovieTitle());
        context.setVariable("theatreName", event.getTheatreName());
        context.setVariable("screenName", event.getScreenName());
        context.setVariable("showTime", event.getShowTime() == null
                ? "" : event.getShowTime().format(SHOW_TIME_FMT));
        context.setVariable("seatLabels", event.getSeatLabels());
        context.setVariable("totalAmount", event.getTotalAmount());
        return templateEngine.process(templateName, context);
    }

    private String templateFor(BookingEventType type) {
        return switch (type) {
            case BOOKING_CONFIRMED -> "booking-confirmed";
            case BOOKING_CANCELLED -> "booking-cancelled";
        };
    }

    private String subjectFor(BookingEvent event) {
        return switch (event.getEventType()) {
            case BOOKING_CONFIRMED -> "Your BookMyShow booking is confirmed - " + event.getMovieTitle();
            case BOOKING_CANCELLED -> "Your BookMyShow booking has been cancelled - " + event.getMovieTitle();
        };
    }
}
