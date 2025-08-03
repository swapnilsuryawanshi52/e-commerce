package com.ecommerce.sbecom.service.impl;

import com.ecommerce.sbecom.entity.*;
import com.ecommerce.sbecom.exception.APIException;
import com.ecommerce.sbecom.exception.ResourceNotFoundException;
import com.ecommerce.sbecom.payload.OrderDTO;
import com.ecommerce.sbecom.payload.OrderItemDTO;
import com.ecommerce.sbecom.repository.*;
import com.ecommerce.sbecom.service.CartService;
import com.ecommerce.sbecom.service.EmailService;
import com.ecommerce.sbecom.service.OrderService;
import com.ecommerce.sbecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;

    @Transactional
    @Override
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod,
                               String pgName, String pgPaymentId, String pgStatus,
                               String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

         User user = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", emailId));

        emailService.sendOrderConfirmation(
                user.getEmail(),
                user.getFirstName(),
                order.getOrderId().toString(),
                order.getTotalAmount().toString(),
                address.getFullAddress()
        );

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();

            product.setQuantity(product.getQuantity() - quantity);

            productRepository.save(product);

            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }

    @Override
    public OrderDTO getOrderById(String emailId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        if (!order.getEmail().equals(emailId)) {
            throw new APIException("Order does not belong to the user with email: " + emailId);
        }
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        if (orderItems.isEmpty()) {
            throw new APIException("No items found for the order with ID: " + orderId);
        }
        List<OrderItemDTO> orderItemDTOS = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
            orderItemDTO.setProductId(orderItem.getProduct().getProductId());
            orderItemDTOS.add(orderItemDTO);
        }
        orderDTO.setOrderItems(orderItemDTOS);
        orderDTO.setAddressId(order.getAddress().getAddressId());
        return orderDTO;
    }

    @Override
    public String cancelOrder(String emailId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        if (!order.getEmail().equals(emailId)) {
            throw new APIException("Order does not belong to the user with email: " + emailId);
        }

        if (!order.getOrderStatus().equalsIgnoreCase("Order Accepted !")) {
            throw new APIException("Order cannot be cancelled as it is already processed or delivered.");
        }

        order.setOrderStatus("Order Cancelled");
        orderRepository.save(order);

        //emailService.sendOrderNotification(order.getEmail(), order.getOrderId().toString(), EmailType.ORDER_CANCELLED);

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }
        return "Order with ID: " + orderId + " has been cancelled successfully.";
    }

    @Override
    public String markOrderShipped(String emailId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setEmailType(EmailType.ORDER_SHIPPED);
        orderRepository.save(order);

        //emailService.sendOrderNotification(order.getEmail(), order.getOrderId().toString(), EmailType.ORDER_SHIPPED);

        return "Order with ID: " + orderId + " has been shipped.";
    }

    @Override
    public String markOrderDelivered(String emailId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setEmailType(EmailType.ORDER_DELIVERED);
        orderRepository.save(order);

        //emailService.sendOrderNotification(order.getEmail(), order.getOrderId().toString(), EmailType.ORDER_DELIVERED);
        return "Order with ID: " + orderId + " has been delivered successfully.";
    }
}