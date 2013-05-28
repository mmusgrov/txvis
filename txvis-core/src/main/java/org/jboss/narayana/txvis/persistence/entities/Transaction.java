package org.jboss.narayana.txvis.persistence.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jboss.narayana.txvis.logparsing.handlers.AbstractHandler;
import org.jboss.narayana.txvis.persistence.enums.EventType;
import org.jboss.narayana.txvis.persistence.enums.Status;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 15/04/2013
 * Time: 14:09
 */
@Entity
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_FLIGHT;

    private Long startTime;

    private Long endTime;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private Collection<ParticipantRecord> participantRecords = new HashSet<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private Collection<Event> events = new HashSet<>();

    protected Transaction() {}

    public Transaction(String transactionId) throws IllegalArgumentException {
        if (!transactionId.matches(AbstractHandler.PATTERN_TXID))
            throw new IllegalArgumentException("Illegal transactionId: " + transactionId);
        this.transactionId = transactionId;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getStartTime() {
        return new Timestamp(startTime);
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime.getTime();
    }

    public Timestamp getEndTime() {
        return new Timestamp(endTime);
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime.getTime();
    }

    public Collection<ParticipantRecord> getParticipantRecords() {
        return this.participantRecords;
    }

    public void addParticipant(ParticipantRecord participantRecord) {
        this.participantRecords.add(participantRecord);
    }

    public Collection<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        this.events.add(event);
        event.setTransaction(this);
    }

    public long getDuration() {
        return status.equals(Status.IN_FLIGHT)
                ? System.currentTimeMillis() - startTime
                : endTime - startTime;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Tx ID: ").append(transactionId);

        for (ParticipantRecord p : participantRecords) {
            result.append("\n\t").append(p);
        }
        return result.toString();
    }
}