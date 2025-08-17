package mekanism.client.lang;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class FormatSplitter {

    //Pattern from Formatter: %[argument_index$][flags][width][.precision][t]conversion
    // Note: This probably supports more formats than MC's formatter does, except things like local translation for I18n seems to
    // go through String.format which would end up using this. So I believe these are technically supported
    // The string pattern from the Formatter is: "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"
    // we modify it to remove the trailing % as MC declares things like %% as invalid
    private static final Pattern fsPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z])");

    public static List<Component> split(String text) {
        Matcher matcher = fsPattern.matcher(text);
        List<Component> components = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            int curStart = matcher.start();
            if (curStart > start) {
                //There is a gap, so we need to grab the piece in between
                components.add(new TextComponent(text.substring(start, curStart)));
            }
            String piece = matcher.group();
            components.add(new FormatComponent(piece));
            start = matcher.end();
        }
        if (start < text.length()) {
            if (start == 0) {
                //If there were no formatting codes, see if there are any potential MessageFormat codes
                // which will get picked up by forge if it could not find any normal formatting codes
                // Note: This assumes every thing between two {} is valid, instead of bothering to check against the set that java + forge declare
                components.addAll(splitMessageFormatInternal(text));
            } else {
                components.add(new TextComponent(text.substring(start)));
            }
        }
        return List.copyOf(components);
    }

    public static List<Component> splitMessageFormat(String text) {
        return List.copyOf(splitMessageFormatInternal(text));
    }

    /**
     * @apiNote Can return two TextComponents neighboring each other it doesn't bother combining them
     */
    private static List<Component> splitMessageFormatInternal(String text) {
        //TODO: Eventually if needed make it combine the neighboring TextComponents
        List<Component> components = new ArrayList<>();
        StringBuilder formattingCode = new StringBuilder();
        StringBuilder rawText = new StringBuilder();
        char[] exploded = text.toCharArray();
        int leftBrackets = 0;
        int firstBracket = -1;
        int secondBracket = -1;
        for (int i = 0; i < exploded.length; i++) {
            char c = exploded[i];
            if (c == '{') {
                if (leftBrackets == 0) {
                    firstBracket = i;
                    String raw = rawText.toString();
                    if (!raw.isEmpty()) {
                        //If we have text and run into a left bracket, then add our text
                        components.add(new TextComponent(raw));
                        rawText = new StringBuilder();
                    }
                } else if (leftBrackets == 1) {
                    secondBracket = i;
                }
                leftBrackets++;
                formattingCode.append(c);
            } else if (leftBrackets > 0) {
                formattingCode.append(c);
                if (c == '}') {
                    leftBrackets--;
                    if (leftBrackets == 0) {
                        //If we finish closing our brackets add our formatting code
                        String piece = formattingCode.toString();
                        MessageFormatComponent component = MessageFormatComponent.fromContents(piece);
                        if (component == null) {
                            if (secondBracket != -1) {
                                //Add the text from the first bracket up to the second bracket as raw text
                                // and reset our state to as if we were starting at that point
                                components.add(new TextComponent(text.substring(firstBracket, secondBracket)));
                                //We use subtract one here so when it is incremented at the end of the loop, it starts in the right place
                                i = secondBracket - 1;
                            } else {
                                //If we only have a depth of one, and it is not a valid message format, then we just add it as a raw string
                                components.add(new TextComponent(piece));
                            }
                        } else {
                            //The string we found represents a valid MessageFormat so add the component for it to our components
                            components.add(component);
                        }
                        //Reset the various variables we use to keep track of where we are
                        formattingCode = new StringBuilder();
                        firstBracket = -1;
                        secondBracket = -1;
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
                components.add(new TextComponent(raw));
            }
        } else {
            if (secondBracket != -1) {
                //At least part of our remaining has a valid MessageFormat representation
                //Add the part before the brackets as raw text
                components.add(new TextComponent(text.substring(firstBracket, secondBracket)));
                //and then try to add the remaining stuff directly
                components.addAll(splitMessageFormatInternal(text.substring(secondBracket)));
            } else {
                //If we don't have a closing bracket, and we didn't have more brackets at some point, add what we have as raw text
                String remainingString = formattingCode.toString();
                if (!remainingString.isEmpty()) {
                    components.add(new TextComponent(remainingString));
                }
            }
        }
        return components;
    }

    public interface Component {

        String contents();
    }

    private record TextComponent(String contents) implements Component {
    }

    public static class FormatComponent implements Component {

        private final String formattingCode;

        private FormatComponent(String formattingCode) {
            this.formattingCode = formattingCode;
        }

        @Override
        public String contents() {
            return formattingCode;
        }
    }

    /**
     * Represents information about a MessageFormat formatting code. Valid MessageFormat styles:
     * <ul>
     * <li> { ArgumentIndex }
     * <li> { ArgumentIndex, FormatType }
     * <li> { ArgumentIndex, FormatType, FormatStyle }
     * </ul>
     */
    public static class MessageFormatComponent extends FormatComponent {

        private final int argumentIndex;
        @Nullable
        private final String formatType;
        @Nullable
        private final String formatStyle;
        private final boolean isChoice;

        private MessageFormatComponent(String contents, int argumentIndex, @Nullable String formatType, @Nullable String formatStyle, boolean isChoice) {
            super(contents);
            this.argumentIndex = argumentIndex;
            this.formatType = formatType;
            this.formatStyle = formatStyle;
            this.isChoice = isChoice;
        }

        public int getArgumentIndex() {
            return argumentIndex;
        }

        /**
         * @apiNote This will not be null if {@link #getFormatStyle()} is not null
         */
        @Nullable
        public String getFormatType() {
            return formatType;
        }

        @Nullable
        public String getFormatStyle() {
            return formatStyle;
        }

        public boolean isChoice() {
            return isChoice;
        }

        /**
         * @param contents Contents to create a {@link MessageFormatComponent} from.
         *
         * @return A {@link MessageFormatComponent} representing the given contents, or {@code null} if the contents do not represent a valid
         * {@link MessageFormatComponent}
         *
         * @see net.neoforged.fml.i18n.FMLTranslations
         */
        @Nullable
        private static MessageFormatComponent fromContents(String contents) {
            int length = contents.length();
            if (length < 3 || contents.charAt(0) != '{' || contents.charAt(length - 1) != '}') {
                //If we don't have at least one digit between the two brackets, or we don't start and end with a bracket
                // then this is not a valid
                return null;
            }
            int firstComma = contents.indexOf(',');
            int argumentIndex;
            try {
                argumentIndex = Integer.parseInt(contents.substring(1, firstComma == -1 ? length - 1 : firstComma));
            } catch (NumberFormatException e) {
                //If the argument is not a valid number, just exit as it is probably not meant to be a
                return null;
            }
            if (argumentIndex < 0 || argumentIndex > 9) {
                //MessageFormat only supports up to 10 total arguments
                return null;
            }
            if (firstComma == -1) {
                //If we don't have a comma, so it is only an argument index we can just exit now
                return new MessageFormatComponent(contents, argumentIndex, null, null, false);
            }
            //Look for the next comma
            int secondComma = contents.indexOf(',', firstComma + 1);
            String formatType = contents.substring(firstComma + 1, secondComma == -1 ? length - 1 : secondComma);
            //Set the format style based on the format type or to null if we do not have one
            String formatStyle = secondComma == -1 ? null : contents.substring(secondComma + 1, length - 1);
            String trimmedFormatType = formatType.trim();
            boolean isChoice = false;
            switch (trimmedFormatType) {
                //Built in Java Format Types
                case "number" -> {
                    if (formatStyle != null && !formatStyle.equals("integer") && !formatStyle.equals("currency") && !formatStyle.equals("percent")) {
                        //If it is not a valid format style for number check if it is a valid SubformatPattern
                        // number uses DecimalFormat as a SubformatPattern
                        try {
                            new DecimalFormat(formatStyle);
                        } catch (IllegalArgumentException e) {
                            //If it is not a valid DecimalFormat then it is not a valid format overall, so return null
                            return null;
                        }
                    }
                }
                case "date", "time" -> {
                    if (formatStyle != null && !formatStyle.equals("short") && !formatStyle.equals("medium") && !formatStyle.equals("long") && !formatStyle.equals("full")) {
                        //If it is not a valid format style for date or time check if it is a valid SubformatPattern
                        // time and date both use SimpleDateFormat as a SubformatPattern
                        try {
                            new SimpleDateFormat(formatStyle, Locale.ENGLISH);
                        } catch (IllegalArgumentException e) {
                            //If it is not a valid SimpleDateFormat then it is not a valid format overall, so return null
                            return null;
                        }
                    }
                }
                case "choice" -> {
                    if (formatStyle == null) {
                        return null;
                    }
                    //Choice is only valid when it has a SubformatPattern, so we return null if we don't have a formatStyle
                    try {
                        new ChoiceFormat(formatStyle);
                    } catch (IllegalArgumentException e) {
                        //If it is not a valid ChoiceFormat to begin with then it is not a valid format overall, so return null
                        return null;
                    }
                    isChoice = true;
                }
                //Forge added Format types
                case "modinfo" -> {
                    if (formatStyle == null || (!formatStyle.equals("id") && !formatStyle.equals("name"))) {
                        //modinfo only supports id, and name, and is not valid if the type is missing
                        return null;
                    }
                }
                case "featurebound", "lower", "upper", "vr", "i18ntranslate" -> {
                    if (formatStyle != null) {
                        //None of these support any format style
                        return null;
                    }
                }
                case "exc" -> {
                    if (formatStyle == null || (!formatStyle.equals("class") && !formatStyle.equals("msg"))) {
                        //exc only supports class, and msg, and is not valid if the type is missing
                        return null;
                    }
                }
                case "i18n", "ornull" -> {
                    if (formatStyle == null) {
                        //i18n, and ornull both require a format style
                        return null;
                    }
                }
                default -> {
                    //Not a valid format type
                    return null;
                }
            }
            return new MessageFormatComponent(contents, argumentIndex, formatType, formatStyle, isChoice);
        }
    }
}