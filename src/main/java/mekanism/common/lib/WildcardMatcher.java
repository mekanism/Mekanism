package mekanism.common.lib;

import java.util.Locale;
import net.minecraft.tags.TagKey;

public class WildcardMatcher {

    private WildcardMatcher() {
    }

    public static boolean matches(String wildcard, TagKey<?> key) {
        return matches(wildcard, key.location().toString());
    }

    public static boolean matches(String wildcard, String text) {
        return matches(wildcard.toLowerCase(Locale.ROOT), text.toLowerCase(Locale.ROOT), 0, 0, false);
    }

    private static boolean matches(String wildcard, String text, int wildcardStartIndex, int textIndex, boolean continueSearch) {
        for (int wildcardIndex = wildcardStartIndex; wildcardIndex < wildcard.length(); wildcardIndex++) {
            char wc = wildcard.charAt(wildcardIndex);
            boolean fail = false;
            if (wc == '*') {
                return matches(wildcard, text, wildcardIndex + 1, textIndex, true);
            } else if (textIndex >= text.length()) {
                return false;
            } else if (wc == '#') {
                if (!Character.isDigit(text.charAt(textIndex))) {
                    fail = true;
                }
            } else if (wc != '?') {
                if (wc != text.charAt(textIndex)) {
                    fail = true;
                }
            }
            if (fail) {
                return continueSearch && matches(wildcard, text, wildcardStartIndex, textIndex + 1, true);
            }
            textIndex++;
        }
        // break if there's more text left, and we didn't end our query with a wildcard char
        return textIndex >= text.length() || (!wildcard.isEmpty() && wildcard.charAt(wildcard.length() - 1) == '*');
    }
}
