package com.ecommerce.notification;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(Map<String, Object> orderEvent) {
        System.out.println("Received order event: " + orderEvent);

        // Process the order event
        long orderId = Long.parseLong(orderEvent.get("orderId").toString());
        String status = orderEvent.get("status").toString();

        System.out.println("Received order event: Order ID: " + orderId + ", Status: " + status);

        // Update Database
        // Send Notification
        // Send Email
        // Generate Invoice
        // Send seller notification
    }
}
