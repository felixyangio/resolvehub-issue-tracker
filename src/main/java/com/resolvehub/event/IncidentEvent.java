package com.resolvehub.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a significant incident lifecycle change occurs.
 *
 * <p>Consumed asynchronously by {@link IncidentEventListener} to send email
 * notifications without blocking the HTTP request thread.</p>
 */
@Getter
public class IncidentEvent extends ApplicationEvent {

    public enum Type {
        CREATED, ASSIGNED, STATUS_CHANGED, COMMENT_ADDED, SLA_BREACHED
    }

    private final Type type;
    private final UUID incidentId;
    private final String incidentTitle;
    private final String recipientEmail;
    private final String recipientName;
    private final String detail;

    public IncidentEvent(Object source, Type type, UUID incidentId, String incidentTitle,
                         String recipientEmail, String recipientName, String detail) {
        super(source);
        this.type = type;
        this.incidentId = incidentId;
        this.incidentTitle = incidentTitle;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.detail = detail;
    }
}
