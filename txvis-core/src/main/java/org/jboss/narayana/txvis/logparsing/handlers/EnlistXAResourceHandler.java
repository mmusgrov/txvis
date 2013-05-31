package org.jboss.narayana.txvis.logparsing.handlers;

import java.util.regex.Matcher;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 28/04/2013
 * Time: 22:43
 */
public class EnlistXAResourceHandler extends AbstractHandler {

    /**
     * RegEx pattern for parsing a participant resource enlist
     *
     * RegEx Groups:
     * 0: The whole matched portion of the log entry
     * 1: The Transaction ID
     * 2: The Resource ID
     */

    public static final String REGEX = "XAResourceRecord\\.XAResourceRecord.+tx_uid=" + PATTERN_TXID
            + ",.+eis name[^>]+>,\\s.*?" + PATTERN_RESOURCEID;


    public EnlistXAResourceHandler() {
        super(REGEX);
    }

    @Override
    public void handle(Matcher matcher, String line) {
        dao.enlistParticipant(matcher.group(TXID), matcher.group(2));
    }

}