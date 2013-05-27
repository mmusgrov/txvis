package org.jboss.narayana.txvis.logparsing.handlers;

import org.jboss.narayana.txvis.Utils;
import org.jboss.narayana.txvis.persistence.entities.Event;
import org.jboss.narayana.txvis.persistence.entities.Transaction;
import org.jboss.narayana.txvis.persistence.enums.EventType;
import org.jboss.narayana.txvis.persistence.enums.Status;

import java.util.regex.Matcher;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 29/04/2013
 * Time: 09:58
 */
public class ClientDrivenRollbackHandler extends AbstractHandler {

    /**
     * RegEx pattern for parsing a client driven rollback
     *
     * RegEx Groups:
     * 0: The whole matched portion of the log entry
     * 1: The Transaction ID
     */
    public static final String REGEX =  TIMESTAMP_PATTEN + ".*?BasicAction::Abort\\(\\)\\sfor\\saction-id\\s" + TX_ID_PATTERN;

    public ClientDrivenRollbackHandler() {
        super(REGEX);
    }

    @Override
    public void handle(Matcher matcher, String line) {
        Transaction t = dao.retrieveTransactionByTxUID(matcher.group(TX_ID));
        t.setStatus(Status.ROLLBACK_CLIENT);
        t.addEvent(new Event(EventType.ABORT, Utils.parseTimestamp(matcher.group(TIMESTAMP))));
        dao.update(t);
    }
}
