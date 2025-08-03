package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.payload.OrderDTO;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod,
                        String pgName, String pgPaymentId, String pgStatus,
                        String pgResponseMessage);

    OrderDTO getOrderById(String emailId, Long orderId);

    String cancelOrder(String emailId, Long orderId);

    String markOrderShipped(String emailId, Long orderId);

    String markOrderDelivered(String emailId, Long orderId);
}