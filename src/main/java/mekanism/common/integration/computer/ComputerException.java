package mekanism.common.integration.computer;

import java.util.Locale;

/**
 * Specialized exception for use in computer methods that doesn't spend time filling in the stack trace as we only really care about the message.
 */
public class ComputerException extends Exception {

    public ComputerException(String message) {
        super(message);
    }

    public ComputerException(String messageFormat, Object... args) {
        this(String.format(Locale.ROOT, messageFormat, args));
    }

    public ComputerException(Exception e) {
        super(e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}