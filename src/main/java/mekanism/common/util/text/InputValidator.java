package mekanism.common.util.text;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.Arrays;
import mekanism.api.functions.CharPredicate;

public class InputValidator {

    private InputValidator() {
    }

    public static final CharPredicate ALL = c -> true;
    public static final CharPredicate DIGIT = c -> c >= '0' && c <= '9';
    public static final CharPredicate LETTER = c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    public static final CharPredicate LETTER_OR_DIGIT = LETTER.or(DIGIT);
    public static final CharPredicate USERNAME = InputValidator.LETTER_OR_DIGIT.or(c -> c == '_');
    public static final CharPredicate RL_NAMESPACE = DIGIT.or(c -> c >= 'a' && c <= 'z').or(from('_', '.', '-'));
    public static final CharPredicate RL_PATH = RL_NAMESPACE.or(from('/'));
    public static final CharPredicate RESOURCE_LOCATION = RL_PATH.or(from(':'));
    public static final CharPredicate DECIMAL = DIGIT.or(from('.'));
    public static final CharPredicate SCI_NOTATION = DECIMAL.or(from('E'));

    public static final CharPredicate WILDCARD_CHARS = from('*', '#', '?');
    public static final CharPredicate FREQUENCY_CHARS = from('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

    public static CharPredicate from(char character) {
        return c -> c == character;
    }

    public static CharPredicate from(char... chars) {
        return new SetInputValidator(chars);
    }

    public static CharPredicate or(CharPredicate... validators) {
        return c -> Arrays.stream(validators).anyMatch(v -> v.test(c));
    }

    public static boolean test(String s, CharPredicate predicate) {
        for (char c : s.toCharArray()) {
            if (!predicate.test(c)) {
                //Invalid name
                return false;
            }
        }
        return true;
    }

    private static class SetInputValidator implements CharPredicate {

        private final CharSet validSet;

        public SetInputValidator(char... chars) {
            validSet = new CharOpenHashSet(chars);
        }

        @Override
        public boolean test(char c) {
            return validSet.contains(c);
        }
    }
}