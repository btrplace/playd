package org.btrplace.playd.model;

import org.btrplace.btrpsl.ErrorMessage;
import org.btrplace.btrpsl.ErrorReporter;
import org.btrplace.btrpsl.ErrorReporterBuilder;
import org.btrplace.btrpsl.Script;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class JSONErrorReporter implements ErrorReporter {

    @Override
    public void append(int lineNo, int colNo, String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ErrorMessage> getErrors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNamespace() {
        throw new UnsupportedOperationException();
    }

    public static class Builder implements ErrorReporterBuilder {

        @Override
        public ErrorReporter build(Script v) {
            return new JSONErrorReporter();
        }
    }

}
