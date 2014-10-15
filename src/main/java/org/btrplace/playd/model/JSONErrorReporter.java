package org.btrplace.playd.model;

import org.btrplace.btrpsl.*;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class JSONErrorReporter extends PlainTextErrorReporter {

    public JSONErrorReporter(Script scr) {
        super(scr);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("[");
        for (Iterator<ErrorMessage> ite = getErrors().iterator(); ite.hasNext(); ) {
            ErrorMessage m = ite.next();
            b.append("{")
                .append("\"ln\": ").append(m.lineNo())
                .append(", \"cn\":").append(m.colNo())
                .append(", \"message\": \"").append(m.message()).append("\"}");
            if (ite.hasNext()) {
                b.append(",");
            }
        }
        return b.append("]").toString();
    }
    public static class Builder implements ErrorReporterBuilder {

        @Override
        public ErrorReporter build(Script v) {
            return new JSONErrorReporter(v);
        }
    }

}
