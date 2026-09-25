package mekanism.client.lang;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.client.lang.FormatSplitter.FormatComponent;
import mekanism.client.lang.FormatSplitter.MessageFormatComponent;

@DisplayName("Test FormatSplitter behavior")
class FormatSplitterTest {

    private static String join(List<Component> components) {
        StringBuilder sb = new StringBuilder();
        for (Component component : components) {
            sb.append(component.contents());
        }
        return sb.toString();
    }

    @Test
    @DisplayName("Empty string yields no components")
    void testEmptyString() {
        Assertions.assertTrue(FormatSplitter.split("").isEmpty());
    }

    @Test
    @DisplayName("Plain text is returned as a single text component")
    void testPlainText() {
        List<Component> components = FormatSplitter.split("hello world");
        Assertions.assertEquals(1, components.size());
        Assertions.assertEquals("hello world", components.get(0).contents());
        Assertions.assertFalse(components.get(0) instanceof FormatComponent);
    }

    @Test
    @DisplayName("String.format placeholders are detected as format components")
    void testStringFormatPlaceholders() {
        List<Component> components = FormatSplitter.split("%s and %d");
        Assertions.assertEquals(3, components.size());
        Assertions.assertTrue(components.get(0) instanceof FormatComponent);
        Assertions.assertEquals("%s", components.get(0).contents());
        Assertions.assertEquals(" and ", components.get(1).contents());
        Assertions.assertTrue(components.get(2) instanceof FormatComponent);
        Assertions.assertEquals("%d", components.get(2).contents());
    }

    @Test
    @DisplayName("Positional String.format placeholders are detected")
    void testPositionalStringFormat() {
        List<Component> components = FormatSplitter.split("%1$s %2$s");
        Assertions.assertEquals(3, components.size());
        Assertions.assertEquals("%1$s", components.get(0).contents());
        Assertions.assertEquals(" ", components.get(1).contents());
        Assertions.assertEquals("%2$s", components.get(2).contents());
    }

    @Test
    @DisplayName("MessageFormat placeholders expose the argument index")
    void testMessageFormatArgumentIndex() {
        List<Component> components = FormatSplitter.split("{0}");
        Assertions.assertEquals(1, components.size());
        Assertions.assertTrue(components.get(0) instanceof MessageFormatComponent);
        MessageFormatComponent mfc = (MessageFormatComponent) components.get(0);
        Assertions.assertEquals(0, mfc.getArgumentIndex());
        Assertions.assertNull(mfc.getFormatType());
    }

    @Test
    @DisplayName("MessageFormat type and style are parsed")
    void testMessageFormatTypeAndStyle() {
        MessageFormatComponent mfc = (MessageFormatComponent) FormatSplitter.split("{0,number,integer}").get(0);
        Assertions.assertEquals("number", mfc.getFormatType());
        Assertions.assertEquals("integer", mfc.getFormatStyle());
    }

    @Test
    @DisplayName("Choice format is detected and marked as a choice")
    void testChoiceFormat() {
        MessageFormatComponent mfc = (MessageFormatComponent) FormatSplitter.split("{0,choice,0#no|1#yes}").get(0);
        Assertions.assertTrue(mfc.isChoice());
        Assertions.assertEquals("choice", mfc.getFormatType());
        Assertions.assertEquals("0#no|1#yes", mfc.getFormatStyle());
    }

    @Test
    @DisplayName("Escaped percent signs are preserved as text")
    void testEscapedPercent() {
        List<Component> components = FormatSplitter.split("Progress: %d%%");
        Assertions.assertEquals(3, components.size());
        Assertions.assertEquals("Progress: ", components.get(0).contents());
        Assertions.assertEquals("%d", components.get(1).contents());
        Assertions.assertEquals("%%", components.get(2).contents());
    }

    @Test
    @DisplayName("Escaped braces are preserved as text")
    void testEscapedBrace() {
        List<Component> components = FormatSplitter.split("{{0}}");
        Assertions.assertEquals(3, components.size());
        Assertions.assertEquals("{", components.get(0).contents());
        Assertions.assertTrue(components.get(1) instanceof MessageFormatComponent);
        Assertions.assertEquals("}", components.get(2).contents());
    }

    @Test
    @DisplayName("Invalid MessageFormat syntax falls back to literal text")
    void testInvalidMessageFormatTreatedAsText() {
        List<Component> components = FormatSplitter.split("{foo}");
        Assertions.assertEquals(1, components.size());
        Assertions.assertEquals("{foo}", components.get(0).contents());
        Assertions.assertFalse(components.get(0) instanceof FormatComponent);
    }

    @Test
    @DisplayName("When a String.format placeholder is present, MessageFormat syntax is treated as literal text")
    void testPercentTriggersStringFormatPath() {
        // split() prefers String.format parsing once a '%' is encountered, so the
        // MessageFormat-style "{0}" is kept as literal text in the same segment.
        List<Component> components = FormatSplitter.split("{0} and %s");
        Assertions.assertEquals(2, components.size());
        Assertions.assertEquals("{0} and ", components.get(0).contents());
        Assertions.assertTrue(components.get(1) instanceof FormatComponent);
        Assertions.assertEquals("%s", components.get(1).contents());
    }

    @Test
    @DisplayName("splitMessageFormat keeps surrounding text and exposes the argument index")
    void testSplitMessageFormat() {
        List<Component> components = FormatSplitter.splitMessageFormat("a{0}b");
        Assertions.assertEquals(3, components.size());
        Assertions.assertEquals("a", components.get(0).contents());
        Assertions.assertTrue(components.get(1) instanceof MessageFormatComponent);
        Assertions.assertEquals(0, ((MessageFormatComponent) components.get(1)).getArgumentIndex());
        Assertions.assertEquals("b", components.get(2).contents());
    }

    @Test
    @DisplayName("Rejoining split components reconstructs the original string")
    void testRoundTripIsLossless() {
        String[] cases = {
                "", "hello", "hello %s", "%s and %d", "%%",
                "{0}", "{0,number}", "{0,number,integer}",
                "{0,choice,0#no|1#yes}", "a {0} b", "hello {",
                "{{0}}", "{0}{1}", "{0,date,short}", "{0,i18n,some.key}",
                "{0,lower}", "{0,modinfo,id}", "Progress: %d%%", "%s {0}",
                "{0} and %s", "text {0,choice,0#none|1#one} end",
                "{", "}", "{}{}", "a}b", "{0}}", "{{1}", "{0,number,@@@}"
        };
        for (String input : cases) {
            Assertions.assertEquals(input, join(FormatSplitter.split(input)),
                    "Round trip failed for input: " + input);
        }
    }

    @Test
    @DisplayName("Round trip is lossless for randomized inputs")
    void testRoundTripIsLosslessFuzz() {
        char[] alphabet = "ab019 ,.{}|#<%$sdntimechoicnumberdatlowerEXC_-".toCharArray();
        Random random = new Random(20260905L);
        for (int i = 0; i < 2000; i++) {
            int len = random.nextInt(16);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                sb.append(alphabet[random.nextInt(alphabet.length)]);
            }
            String input = sb.toString();
            Assertions.assertEquals(input, join(FormatSplitter.split(input)),
                    "Round trip failed for input: " + input);
        }
    }
}
