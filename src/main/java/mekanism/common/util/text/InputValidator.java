package mekanism.common.util.text;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.function.IntPredicate;
import net.minecraft.resources.Identifier;

public class InputValidator {

    private InputValidator() {
    }

    //int == codepoint - note that for Character.isBmpCodePoint(c) == true then the integer value is the same as char
    public static final IntPredicate ALL = c -> true;
    public static final IntPredicate DIGIT = c -> c >= '0' && c <= '9';
    public static final IntPredicate DIGIT_OR_NEGATIVE = DIGIT.or(c -> c == '-');
    public static final IntPredicate LETTER = c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    public static final IntPredicate LETTER_OR_DIGIT = LETTER.or(DIGIT);
    //Note: This is stricter than StringUtil#isValidPlayerName, but follows what is valid based on mojang's support site
    public static final IntPredicate USERNAME = LETTER_OR_DIGIT.or(c -> c == '_');
    public static final IntPredicate RL_NAMESPACE = c -> Character.isBmpCodePoint(c) && Identifier.validNamespaceChar((char) c);
    public static final IntPredicate RL_PATH = c -> Character.isBmpCodePoint(c) && Identifier.validPathChar((char) c);
    public static final IntPredicate RESOURCE_LOCATION = c -> Character.isBmpCodePoint(c) && Identifier.isAllowedInIdentifier((char) c);
    public static final IntPredicate DECIMAL = DIGIT.or(from('.'));
    public static final IntPredicate SCI_NOTATION = DECIMAL.or(from('E'));

    public static final IntPredicate WILDCARD_CHARS = from('*', '#', '?');
    public static final IntPredicate FREQUENCY_CHARS = from('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

    public static IntPredicate from(char character) {
        return c -> Character.isBmpCodePoint(c) && c == character;
    }

    public static IntPredicate from(char... chars) {
        return new SetInputValidator(chars);
    }

    public static IntPredicate or(IntPredicate... validators) {
        if (validators.length == 1) {
            return validators[0];
        }
        return c -> {
            for (IntPredicate v : validators) {
                if (v.test(c)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static boolean test(String s, IntPredicate predicate) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!predicate.test(s.codePointAt(i))) {
                //Invalid name
                return false;
            }
        }
        return true;
    }

    private static class SetInputValidator implements IntPredicate {

        private final CharSet validSet;

        public SetInputValidator(char... chars) {
            validSet = new CharOpenHashSet(chars);
        }

        @Override
        public boolean test(int c) {
            return Character.isBmpCodePoint(c) && validSet.contains((char) c);
        }
    }
}