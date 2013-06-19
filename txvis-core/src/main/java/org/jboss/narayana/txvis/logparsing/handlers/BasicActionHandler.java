package org.jboss.narayana.txvis.logparsing.handlers;

import java.sql.Timestamp;
import java.util.regex.Matcher;

/**
 *
 * An example log line is as follows:
 * <code>>18:15:54,161 TRACE [com.arjuna.ats.arjuna] (default task-14) BasicAction::Begin() for action-id 0:ffffac1182da:579fdb56:51a46622:e77</code>
 *
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 28/05/2013
 * Time: 19:34
 */
public class BasicActionHandler extends AbstractHandler {

    public static final String REGEX = PATTERN_TIMESTAMP +
            ".*?BasicAction::(?<BASICACTION>Begin|End|Abort|phase2Abort|onePhaseCommit)\\(\\)\\sfor\\saction-id\\s"
            + PATTERN_TXID;

    public BasicActionHandler() {
        super(REGEX);
    }

    @Override
    public void handle(Matcher matcher, String line) {
        final String txuid = matcher.group(TXID);
        final Timestamp timestamp =  parseTimestamp(matcher.group(TIMESTAMP));

        switch (matcher.group("BASICACTION")) {
            case "Begin":
                service.createTx(txuid, timestamp);
                break;
            case "End":
                service.prepareTx(txuid, timestamp);
                break;
            case "Abort":
                service.topLevelAbortTx(txuid, timestamp);
                break;
            case "phase2Abort":
                service.resourceDrivenAbortTx(txuid, timestamp);
                break;
            case "onePhaseCommit":
                service.commitTx1Phase(txuid, timestamp);
                break;
        }
    }
}
