package guru.springframework.spring7restmvc.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import guru.springframework.spring7restmvc.config.KafkaConfig;

/**
 * Created by jt, Spring Framework Guru.
 */
@Component
public class OrderPlacedKafkaListener {
    AtomicInteger messageCounter = new AtomicInteger(0);

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.ORDER_PLACED_TOPIC)
    public void receive(OrderPlacedEvent orderPlacedEvent) {
        System.out.println("Received Message: " + orderPlacedEvent);
        messageCounter.incrementAndGet();
    }

}