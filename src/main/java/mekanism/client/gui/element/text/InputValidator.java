package mekanism.client.gui.element.text;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.Arrays;
import mekanism.api.functions.CharPredicate;

public class InputValidator {

    private InputValidator() {
    }

    public static final CharPredicate ALL = c -> true;
    public static final CharPredicate DIGIT = Character::isDigit;
    public static final CharPredicate LETTER = Character::isLetter;
    public static final CharPredicate DECIMAL = DIGIT.or(from('.'));
    public static final CharPredicate SCI_NOTATION = DECIMAL.or(from('E'));

    public static final CharPredicate FILTER_CHARS = from('*', '-', ' ', '|', '_', '\'', ':', '/', '#', '?');
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