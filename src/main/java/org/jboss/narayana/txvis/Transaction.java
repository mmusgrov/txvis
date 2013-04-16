package org.jboss.narayana.txvis;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 15/04/2013
 * Time: 14:09
 */
public class Transaction {

    private String txId;
    private List<Participant> participants = new LinkedList<>();

    public Transaction(String txId) throws IllegalArgumentException, NullPointerException {
        if (!txId.matches(Patterns.TX_ID))
            throw new IllegalArgumentException("Invalid transaction ID: " + txId);

        this.txId = txId;
    }

    public void addParticipant(Participant participant) throws NullPointerException {
        if(participant == null)
            throw new NullPointerException("Expected participant");
        participants.add(participant);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Tx ID: ").append(txId);

        for(Participant p : participants) {
            result.append("\n\t").append(p);
        }

        return result.toString();
    }

}
