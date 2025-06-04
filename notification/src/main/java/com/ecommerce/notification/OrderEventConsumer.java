package com.ecommerce.notification;


import com.ecommerce.notification.payload.OrderConfirmEvent;
import com.ecommerce.notification.payload.OrderStatus;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class OrderEventConsumer {

//    @RabbitListener(queues = "${rabbitmq.queue.name}")
//    public void handleOrderEvent(OrderConfirmEvent orderConfirmEvent) {
//        System.out.println("Received order event: " + orderConfirmEvent);
//
//        // Process the order event
//        long orderId = orderConfirmEvent.getOrderId();
//        OrderStatus status = orderConfirmEvent.getStatus();
//
//        System.out.println("Received order event: Order ID: " + orderId + ", Status: " + status);
//
//        // Update Database
//        // Send Notification
//        // Send Email
//        // Generate Invoice
//        // Send seller notification
//    }

        @Bean
        public Consumer<OrderConfirmEvent> orderConfirmEventConsumer() {
            return orderConfirmEvent -> {
                log.info("Received order event: {}", orderConfirmEvent);

                // Process the order event
                long orderId = orderConfirmEvent.getOrderId();
                OrderStatus status = orderConfirmEvent.getStatus();

                log.info("Received order event: Order ID: {}, Status: {}", orderId, status);

                // Update Database
                // Send Notification
                // Send Email
                // Generate Invoice
                // Send seller notification
            };
        }
}
