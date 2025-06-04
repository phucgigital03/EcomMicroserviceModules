package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderConfirmEvent;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
//    private final RabbitTemplate rabbitTemplate;
    private final StreamBridge streamBridge;

//    @Value("${rabbitmq.exchange.name}")
//    private String exchangeName;
//
//    @Value("${rabbitmq.routing.key}")
//    private String routingKey;

    public Optional<OrderResponse> createOrder(String userId) {
        // Validate for cart items
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) {
            return Optional.empty(); // No items in cart
        }
        // Validate for user
//        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
//        if (userOpt.isEmpty()) {
//            return Optional.empty();
//        }
//        User user = userOpt.get();

        // Calculate total price
        BigDecimal totalPrice = cartItems.stream()
                .map(cartItem ->{
                    BigDecimal price = cartItem.getPrice();
                    return price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getQuantity(),
                        item.getPrice(),
                        order,
                        item.getProductId()
                ))
                .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear cart items
        cartService.clearCart(userId);

        // Create OrderResponse
        OrderResponse orderResponse;
        orderResponse = modelMapper.map(savedOrder, OrderResponse.class);
        orderResponse.getItems().forEach(item -> {
            item.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        });

        OrderConfirmEvent event = new OrderConfirmEvent(
                orderResponse.getId(),
                savedOrder.getUserId(),
                orderResponse.getStatus(),
                orderResponse.getItems(),
                orderResponse.getTotalAmount(),
                orderResponse.getCreatedAt()
        );
//        rabbitTemplate.convertAndSend(exchangeName, routingKey,
//               event
//        );

        streamBridge.send("createOrder-out-0", event);

        return Optional.of(orderResponse);
    }
}
