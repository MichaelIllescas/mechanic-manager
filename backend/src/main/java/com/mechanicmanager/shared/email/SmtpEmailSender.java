package com.mechanicmanager.shared.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class SmtpEmailSender implements EmailSender {
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String fromAddress;
    private final String fromName;
    
    public SmtpEmailSender(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${spring.mail.username}") String fromAddress,
            @Value("${app.mail.from-name}") String fromName
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }
    
    @Override
    @Async(EmailAsyncConfig.EMAIL_EXECUTOR)
    public void send(EmailMessage message) {
        try {
            Context context = new Context();
            context.setVariables(message.variables());
            String html = templateEngine.process("email/" + message.template(), context);
            
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(html, true);
            
            mailSender.send(mime);
            log.info("Email enviado. template={}", message.template());
        } catch (Exception e) {
            log.error("Fallo el envio de email. template={}", message.template(), e);
            throw new EmailDeliveryException(e);
        }
    }
}