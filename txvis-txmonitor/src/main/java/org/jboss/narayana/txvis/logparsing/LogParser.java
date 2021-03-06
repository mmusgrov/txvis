package org.jboss.narayana.txvis.logparsing;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.log4j.Logger;
import org.jboss.narayana.txvis.logparsing.handlers.Handler;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 25/04/2013
 * Time: 16:49
 */
public final class LogParser implements TailerListener {

    private static final Logger logger = Logger.getLogger("org.jboss.narayana.txvis");
    private final List<Handler> handlers = new LinkedList<Handler>();
    private Tailer tailer;

    // Enforce package-private constructor
    LogParser() {}

    void addHandler(Handler lineHandler) throws NullPointerException {
        if (lineHandler == null)
            throw new NullPointerException("null param lineHandler");
        handlers.add(lineHandler);
    }

    @Override
    public void handle(String line) {
        /*
         * Checks that the log line was not created by the current thread, this prevents
         * an infinite loop if the persistence layer is running in the same JBoss instance
         * that it is monitoring. This happens because the container creates its own transaction
         * to persist to the database, the log parser will parse this transaction, which in turn
         * creates it's own transaction when persisting the data... repeat ad infinitum.
         *
         */
        if(filter(line))
            return;

        for (Handler handler : handlers) {
            Matcher matcher = handler.getPattern().matcher(line);
            if (matcher.find()) {

                if (logger.isDebugEnabled())
                    logger.debug(logFormat(handler, matcher));

                handler.handle(matcher, line);
                break;
            }
        }
    }

    private boolean filter(String line) {
        final String threadId = "\\((pool-\\d+-thread-\\d+)\\)";
        Matcher matcher = Pattern.compile(threadId).matcher(line);

        if (matcher.find()) {
            if (Thread.currentThread().getName().equals(matcher.group(1))) {
                if (logger.isTraceEnabled())
                    logger.trace("Filter match, dropped line " + line);
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(Tailer tailer) {
        this.tailer = tailer;
    }

    @Override
    public void fileNotFound() {
        logger.fatal("Log file not found: " + tailer.getFile());
    }

    @Override
    public void fileRotated() {}

    @Override
    public void handle(Exception ex) {
        logger.error("Exception thrown while parsing logfile", ex);
    }


    private String logFormat(Handler handler, Matcher matcher) {
        StringBuilder sb =
                new StringBuilder(this + " Parser match: handler=").append(handler.getClass().getName());

        for (int i = 1; i <= matcher.groupCount(); i++)
            sb.append(", matcher.group(").append(i).append(")=").append(matcher.group(i));

        return sb.toString();
    }
}