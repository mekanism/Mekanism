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
            if (start == 0) {
                //If there were no formatting codes, see if there are any potential MessageFormat codes
                // which will get picked up by forge if it could not find any normal formatting codes
                // Note: This assumes every thing between two {} is valid, instead of bothering to check against the set that java + forge declare
                components.addAll(splitMessageFormat(text));
            } else {
                components.add(new Component(text.substring(start), false));
            }
        }
        return ImmutableList.copyOf(components);
    }

    private static List<Component> splitMessageFormat(String text) {
        List<Component> components = new ArrayList<>();
        StringBuilder formattingCode = new StringBuilder();
        StringBuilder rawText = new StringBuilder();
        char[] exploded = text.toCharArray();
        int leftBrackets = 0;
        int maxCurBrackets = 0;
        for (char c : exploded) {
            if (c == '{') {
                if (leftBrackets == 0) {
                    String raw = rawText.toString();
                    if (!raw.isEmpty()) {
                        //If we have text and run into a left bracket, then add our text
                        components.add(new Component(raw, false));
                        rawText = new StringBuilder();
                    }
                }
                leftBrackets++;
                maxCurBrackets++;
                formattingCode.append(c);
            } else if (leftBrackets > 0) {
                formattingCode.append(c);
                if (c == '}') {
                    leftBrackets--;
                    if (leftBrackets == 0) {
                        //If we finish closing our brackets add our formatting code
                        components.add(new Component(formattingCode.toString(), true));
                        formattingCode = new StringBuilder();
                        maxCurBrackets = 0;
                    }
                }
            } else {
                rawText.append(c);
            }
        }
        if (leftBrackets == 0) {
            String raw = rawText.toString();
            if (!raw.isEmpty()) {
                //Add any remaining text
                components.add(new Component(raw, false));
            }
        } else {
            String remainingString = formattingCode.toString();
            if (maxCurBrackets > leftBrackets && remainingString.length() > 1) {
                //At least part of our remaining has a valid MessageFormat representation
                List<Component> subComponents = splitMessageFormat(remainingString.substring(1));
                Component firstComponent = subComponents.get(0);
                if (firstComponent.isFormattingCode()) {
                    components.add(new Component("{", false));
                    components.addAll(subComponents);
                } else {
                    components.add(new Component("{" + firstComponent.getContents(), false));
                    for (int i = 1; i < subComponents.size(); i++) {
                        components.add(subComponents.get(i));
                    }
                }
            } else {
                //If we don't have a closing bracket and we didn't have more brackets at some point, add what we have as raw text
                components.add(new Component(remainingString, false));
            }
        }
        return components;
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