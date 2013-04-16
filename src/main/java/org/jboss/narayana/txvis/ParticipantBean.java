package org.jboss.narayana.txvis;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 16/04/2013
 * Time: 14:49
 */
public class ParticipantBean {

    private final Map<String, Participant> participants = new HashMap<>();

    public Participant getParticipant(String participantID)
            throws IllegalArgumentException, NullPointerException {
        Participant participant = this.participants.get(participantID);

        if (participant == null) {
            participant = new Participant(participantID);
            this.participants.put(participantID, participant);
        }

        return participant;
    }



}
