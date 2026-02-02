package com.tcon.financial_service.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

    @KafkaListener(topics = "booking-cancelled", groupId = "financial-service-group")
    public void handleBookingCancelled(Map<String, Object> event) {
        log.info("Received booking cancelled event: {}", event);

        String bookingId = (String) event.get("bookingId");
        Integer hoursBeforeClass = (Integer) event.get("hoursBeforeClass");

        // Process refund if payment exists
        // This would trigger refund processing based on cancellation policy

        log.info("Processing refund for cancelled booking: {}", bookingId);
    }

    @KafkaListener(topics = "class-completed", groupId = "financial-service-group")
    public void handleClassCompleted(Map<String, Object> event) {
        log.info("Received class completed event: {}", event);

        String bookingId = (String) event.get("bookingId");
        String teacherId = (String) event.get("teacherId");

        // Mark payment as eligible for payout after class completion

        log.info("Class completed, payment eligible for payout: {}", bookingId);
    }
}
