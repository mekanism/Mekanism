package mekanism.client.lang;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatSplitter {

    //Pattern from Formatter: %[argument_index$][flags][width][.precision][t]conversion
    // Note: This probably supports more formats than MC's formatter does, except things like local translation for I18n seems to
    // go through String.format which would end up using this. So I believe these are technically supported
    // The string pattern from the Formatter is: "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"
    // we modify it to remove the trailing % as MC declares things like %% as invalid
    private static final Pattern fsPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z])");

    public static List<Component> split(String text) {
        Matcher matcher = fsPattern.matcher(text);
        List<Component> components = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            int curStart = matcher.start();
            if (curStart > start) {
                //There is a gap so we need to grab the piece in between
                components.add(new Component(text.substring(start, curStart), false));
            }
            String piece = matcher.group();
            components.add(new Component(piece, true));
            start = matcher.end();
        }
        if (start < text.length()) {
            components.add(new Component(text.substring(start), false));
        }
        return ImmutableList.copyOf(components);
    }

    public static class Component {

        private final String contents;
        private final boolean isFormattingCode;

        private Component(String contents, boolean isFormattingCode) {
            this.contents = contents;
            this.isFormattingCode = isFormattingCode;
        }

        public String getContents() {
            return contents;
        }

        public boolean isFormattingCode() {
            return isFormattingCode;
        }
    }
}