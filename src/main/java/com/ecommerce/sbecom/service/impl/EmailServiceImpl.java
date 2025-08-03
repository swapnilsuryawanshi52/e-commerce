package com.ecommerce.sbecom.service.impl;

import com.ecommerce.sbecom.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void sendOrderConfirmation(String toEmail, String userName, String orderId,
                                      String orderAmount, String shippingAddress) {
        try {
            Context context = new Context();

            context.setVariable("firstName", userName);
            context.setVariable("orderId", orderId);
            context.setVariable("orderAmount", orderAmount);
            context.setVariable("shippingAddress", shippingAddress);

            String body = templateEngine.process("email/order-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Your Order Confirmation");
            helper.setText(body, true); // true for HTML

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending order confirmation", e);
        }
    }


//    private String loadTemplate(String path) throws IOException {
//        ClassPathResource resource = new ClassPathResource(path);
//        byte[] bytes = resource.getInputStream().readAllBytes();
//        return new String(bytes, StandardCharsets.UTF_8);
//    }
}
