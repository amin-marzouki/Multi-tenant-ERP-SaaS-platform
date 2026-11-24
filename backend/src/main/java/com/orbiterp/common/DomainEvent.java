package com.orbiterp.common;

/**
 * An event one module publishes for the others. Modules only talk to each other through
 * these events (published with Spring's ApplicationEventPublisher), never through each
 * other's repositories. Listeners run synchronously in the publisher's transaction, so a
 * listener that throws rolls the whole change back.
 */
public interface DomainEvent {

    Long tenantId();
}
