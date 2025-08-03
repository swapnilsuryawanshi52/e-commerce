package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.entity.EmailType;

public interface EmailService {
    void sendOrderConfirmation(String toEmail, String userName, String orderId, String totalAmount, String shippingAddress);
}
