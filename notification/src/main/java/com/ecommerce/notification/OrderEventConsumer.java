package com.ecommerce.notification;


import com.ecommerce.notification.payload.OrderConfirmEvent;
import com.ecommerce.notification.payload.OrderStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderConfirmEvent orderConfirmEvent) {
        System.out.println("Received order event: " + orderConfirmEvent);

        // Process the order event
        long orderId = orderConfirmEvent.getOrderId();
        OrderStatus status = orderConfirmEvent.getStatus();

        System.out.println("Received order event: Order ID: " + orderId + ", Status: " + status);

        // Update Database
        // Send Notification
        // Send Email
        // Generate Invoice
        // Send seller notification
    }
}
