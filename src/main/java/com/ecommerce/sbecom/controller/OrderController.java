package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.payload.OrderDTO;
import com.ecommerce.sbecom.payload.OrderRequestDTO;
import com.ecommerce.sbecom.service.OrderService;
import com.ecommerce.sbecom.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        System.out.println("orderRequestDTO DATA: " + orderRequestDTO);
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderDTO> getOrderById(@RequestParam Long orderId) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.getOrderById(emailId, orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        String emailId = authUtil.loggedInEmail();
        orderService.cancelOrder(emailId, orderId);
        return ResponseEntity.ok("Order cancelled and email sent.");
    }

    @PutMapping("/orders/{orderId}/ship")
    public ResponseEntity<String> shipOrder(@PathVariable Long orderId) {
        String emailId = authUtil.loggedInEmail();
        orderService.markOrderShipped(emailId, orderId);
        return ResponseEntity.ok("Order marked as shipped and email sent.");
    }

    @PutMapping("orders/{orderId}/deliver")
    public ResponseEntity<String> deliverOrder(@PathVariable Long orderId) {
        String emailId = authUtil.loggedInEmail();
        orderService.markOrderDelivered(emailId, orderId);
        return ResponseEntity.ok("Order delivered and email sent.");
    }
}
