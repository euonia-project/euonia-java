package com.euonia.domain.event;

import com.euonia.bus.message.Multicast;

/**
 * The Event interface represents a generic event in the domain model. It defines the basic properties and methods that all events should have.
 * Events are used to capture and represent significant occurrences or changes in the system, allowing for communication and coordination between different components.
 */
public interface Event extends Multicast {
    /**
     * Gets the sequence number of the event, which can be used to determine the order of events.
     *
     * @return the sequence number of the event
     */
    long getSequence();

    /**
     * Sets the sequence number of the event, which can be used to determine the order of events.
     *
     * @param sequence the sequence number of the event
     */
    void setSequence(long sequence);

    /**
     * Gets the event intent, which represents the purpose or meaning of the event.
     * It can be used to categorize or identify the type of event being processed.
     *
     * @return the intent of the event
     */
    String getEventIntent();

    /**
     * Sets the event intent, which represents the purpose or meaning of the event.
     * It can be used to categorize or identify the type of event being processed.
     *
     * @param eventIntent the intent of the event
     */
    void setEventIntent(String eventIntent);

    /**
     * Gets the originator type of the event, which indicates the source or origin of the event.
     *
     * @return the originator type of the event
     */
    String getOriginatorType();

    /**
     * Sets the originator type of the event, which indicates the source or origin of the event.
     *
     * @param originatorType the originator type of the event
     */
    void setOriginatorType(String originatorType);

    /**
     * Gets the originator ID of the event, which uniquely identifies the source or origin of the event.
     *
     * @return the originator ID of the event
     */
    String getOriginatorId();

    /**
     * Sets the originator ID of the event, which uniquely identifies the source or origin of the event.
     *
     * @param originatorId the originator ID of the event
     */
    void setOriginatorId(String originatorId);
}
