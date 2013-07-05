package org.jboss.narayana.txvis.logparsing.common;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 25/04/2013
 * Time: 16:49
 */
public final class LogParser implements TailerListener {

    private static final Logger logger = Logger.getLogger(LogParser.class.getName());
    private final List<Handler> handlers = new LinkedList<>();
    private Tailer tailer;

    // Enforce package-private constructor
    LogParser() {}

    /**
     *
     * @param lineHandler
     * @throws NullPointerException
     */
    void addHandler(Handler lineHandler) throws NullPointerException {
        if (lineHandler == null)
            throw new NullPointerException("Method called with null parameter: lineHandler");
        handlers.add(lineHandler);
    }

    /**
     *
     * @param line
     */
    @Override
    public void handle(String line) {
        for (Handler handler : handlers) {
            final Matcher matcher = handler.getPattern().matcher(line);

            if (matcher.find()) {
                if (logger.isDebugEnabled())
                    logger.debug(logFormat(handler, matcher));

                handler.handle(matcher, line);
                break;
            }
        }
    }

    /**
     *
     * @param tailer
     */
    @Override
    public void init(Tailer tailer) {
        this.tailer = tailer;
    }

    /**
     *
     */
    @Override
    public void fileNotFound() {
        logger.fatal("Log file not found: " + tailer.getFile());
        throw new IllegalStateException("Log file not found: " + tailer.getFile());
    }

    /**
     *
     */
    @Override
    public void fileRotated() {
        if (logger.isInfoEnabled())
            logger.info("Log file has been rotated");
    }

    /**
     *
     * @param ex
     */
    @Override
    public void handle(Exception ex) {
        logger.error("Exception caught: ", ex);
        throw new RuntimeException(ex);
    }

    /*
     *
     */
    private String logFormat(Handler handler, Matcher matcher) {
        final StringBuilder sb =
                new StringBuilder(this + " Parser match: handler=`").append(handler.getClass().getSimpleName());

        for (int i = 1; i <= matcher.groupCount(); i++)
            sb.append("`, matcher.group(").append(i).append(")=`").append(matcher.group(i));

        return sb.append("`").toString();
    }
}