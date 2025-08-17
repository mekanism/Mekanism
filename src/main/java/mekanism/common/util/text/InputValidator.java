package mekanism.common.util.text;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import mekanism.api.functions.CharPredicate;
import net.minecraft.resources.ResourceLocation;

public class InputValidator {

    private InputValidator() {
    }

    public static final CharPredicate ALL = c -> true;
    public static final CharPredicate DIGIT = c -> c >= '0' && c <= '9';
    public static final CharPredicate DIGIT_OR_NEGATIVE = DIGIT.or(c -> c == '-');
    public static final CharPredicate LETTER = c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    public static final CharPredicate LETTER_OR_DIGIT = LETTER.or(DIGIT);
    //Note: This is stricter than StringUtil#isValidPlayerName, but follows what is valid based on mojang's support site
    public static final CharPredicate USERNAME = LETTER_OR_DIGIT.or(c -> c == '_');
    public static final CharPredicate RL_NAMESPACE = ResourceLocation::validNamespaceChar;
    public static final CharPredicate RL_PATH = ResourceLocation::validPathChar;
    public static final CharPredicate RESOURCE_LOCATION = ResourceLocation::isAllowedInResourceLocation;
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
        if (validators.length == 1) {
            return validators[0];
        }
        return c -> {
            for (CharPredicate v : validators) {
                if (v.test(c)) {
                    return true;
                }
            }
            return false;
        };
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