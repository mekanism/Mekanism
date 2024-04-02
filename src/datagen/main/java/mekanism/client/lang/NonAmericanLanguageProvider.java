package mekanism.client.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.client.lang.FormatSplitter.FormatComponent;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;

public class NonAmericanLanguageProvider extends ConvertibleLanguageProvider {

    private static final List<WordConversion> CONVERSIONS = Util.make(new HashMap<String, String>(), map -> {
        addEntry(map, "Pressurized", "Pressurised");
        addEntry(map, "Stabilizer", "Stabiliser");
        addEntry(map, "Stabilizing", "Stabilising");
        addEntry(map, "Stabilization", "Stabilisation");
        addEntry(map, "Crystallizer", "Crystalliser");
        addEntry(map, "Nucleosynthesizer", "Nucleosynthesiser");
        addEntry(map, "Color", "Colour");
        addEntry(map, "Oxidizer", "Oxidiser");
        addEntry(map, "Oxidizing", "Oxidising");
        addEntry(map, "Formulas", "Formulae");
        addEntry(map, "Energized", "Energised");
        addEntry(map, "Energize", "Energise");
        addEntry(map, "Energizing", "Energising");
        addEntry(map, "Utilizes", "Utilises");
        addEntry(map, "Motorized", "Motorised");
        addEntry(map, "Bodyarmor", "Bodyarmour");
        addEntry(map, "Armor", "Armour");
        addEntry(map, "Armored", "Armoured");
        addEntry(map, "Gray", "Grey");
        addEntry(map, "Whooshes", "Wooshes");
    }).entrySet().stream().map(entry -> new WordConversion(entry.getKey(), entry.getValue())).toList();

    private static void addEntry(Map<String, String> map, String key, String value) {
        map.put(key, value);
        map.put(key.toLowerCase(Locale.ROOT), value.toLowerCase(Locale.ROOT));
    }

    public NonAmericanLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    public void convert(String key, String raw, List<Component> splitEnglish) {
        StringBuilder builder = new StringBuilder();
        boolean foundMatch = false;
        for (Component component : splitEnglish) {
            if (component instanceof FormatComponent) {
                builder.append(component.contents());
            } else {
                String contents = component.contents();
                List<WordConversion> matched = new ArrayList<>();
                for (WordConversion conversion : CONVERSIONS) {
                    if (conversion.match(contents).find()) {
                        matched.add(conversion);
                    }
                }
                if (!matched.isEmpty()) {
                    foundMatch = true;
                    for (WordConversion conversion : matched) {
                        contents = conversion.replace(contents);
                    }
                }
                builder.append(contents);
            }
        }
        if (foundMatch) {
            add(key, builder.toString());
        }
    }

    private record WordConversion(Pattern matcher, String replacement) {

        private WordConversion(String toReplace, String replacement) {
            this(Pattern.compile("\\b" + toReplace + "\\b"), replacement);
        }

        public Matcher match(String contents) {
            return matcher.matcher(contents);
        }

        public String replace(String contents) {
            return match(contents).replaceAll(replacement);
        }
    }
}
