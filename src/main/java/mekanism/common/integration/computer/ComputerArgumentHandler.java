package mekanism.common.integration.computer;

import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Short-lived helper to wrap handling of arguments for different computer types and build up any implicit casts needed
 */
public abstract class ComputerArgumentHandler<EXCEPTION extends Exception, RESULT> {

    public abstract int getCount();

    protected String formatError(String messageFormat, Object... args) {
        return String.format(Locale.ROOT, messageFormat, args);
    }

    public abstract EXCEPTION error(String messageFormat, Object... args);

    @Nullable
    public abstract Object getArgument(int index);

    /**
     * Allows implementers to override and add in various extra "implicit" casts that are not in Java's default conversions. This method will only be called if the
     * conversion is not already valid, this also means that expectedType will never be equal to argumentType.
     *
     * @return An object representing the sanitized/converted version of the argument, or the passed in argument if no conversion occurred.
     */
    public Object sanitizeArgument(Class<?> expectedType, Class<?> argumentType, Object argument) {
        return argument;
    }

    public abstract RESULT noResult();

    public abstract RESULT wrapResult(Object result);
}