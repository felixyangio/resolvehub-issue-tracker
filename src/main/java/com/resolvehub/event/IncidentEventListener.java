package com.resolvehub.event;

import com.resolvehub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Asynchronous listener for incident lifecycle events.
 *
 * <p>Runs on a separate thread pool so email I/O never blocks the HTTP
 * response. If delivery fails the error is logged but the original
 * transaction is not rolled back — eventual consistency is acceptable
 * for notifications.</p>
 *
 * <p><strong>Interview note:</strong> This demonstrates the Observer pattern
 * via Spring Events, decoupling the notification concern from the core
 * business service. The @Async annotation moves execution to a thread
 * pool managed by Spring's TaskExecutor.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleIncidentEvent(IncidentEvent event) {
        log.info("Handling {} event for incident {} (recipient: {})",
                event.getType(), event.getIncidentId(), event.getRecipientEmail());

        try {
            switch (event.getType()) {
                case ASSIGNED -> notificationService.sendAssignmentNotification(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getIncidentId(), event.getIncidentTitle(), event.getDetail());
                case STATUS_CHANGED -> notificationService.sendStatusChangeNotification(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getIncidentId(), event.getIncidentTitle(), event.getDetail());
                case COMMENT_ADDED -> notificationService.sendCommentNotification(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getIncidentId(), event.getIncidentTitle(), event.getDetail());
                case SLA_BREACHED -> notificationService.sendSlaBreachNotification(
                        event.getRecipientEmail(), event.getRecipientName(),
                        event.getIncidentId(), event.getIncidentTitle(), event.getDetail());
                default -> log.debug("No handler for event type: {}", event.getType());
            }
        } catch (Exception e) {
            // Never let notification failure propagate — log and move on
            log.error("Failed to send notification for event {}: {}", event.getType(), e.getMessage(), e);
        }
    }
}
