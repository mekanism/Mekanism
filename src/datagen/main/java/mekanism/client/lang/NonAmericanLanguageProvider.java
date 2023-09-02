package mekanism.client.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.client.lang.FormatSplitter.FormatComponent;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;

public class NonAmericanLanguageProvider extends ConvertibleLanguageProvider {
    private static final List<Map.Entry<Pattern, String>> CONVERSIONS = Util.make(new HashMap<String, String>(), map ->{
        addEntry(map, "Pressurized", "Pressurised");
        addEntry(map, "Stabilizer", "Stabiliser");
        addEntry(map, "Stabilizing", "Stabilising");
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
    }).entrySet().stream().map(entry->Map.entry(Pattern.compile("\\b"+entry.getKey()+"\\b"), entry.getValue())).toList();

    private static void addEntry(Map<String, String> map, String key, String value) {
        map.put(key, value);
        map.put(key.toLowerCase(), value.toLowerCase());
    }

    public NonAmericanLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    public void convert(String key, List<Component> splitEnglish) {
        StringBuilder builder = new StringBuilder();
        boolean foundMatch = false;
        for (Component component : splitEnglish) {
            if (component instanceof FormatComponent) {
                builder.append(component.contents());
            } else {
                String contents = component.contents();
                String finalContents = contents;
                List<Entry<Pattern, String>> matched = CONVERSIONS.stream().filter(e->e.getKey().matcher(finalContents).find()).toList();
                if (!matched.isEmpty()) {
                    foundMatch = true;
                    for (Entry<Pattern, String> entry : matched) {
                        contents = entry.getKey().matcher(contents).replaceAll(entry.getValue());
                    }
                }
                builder.append(contents);
            }
        }
        if (foundMatch) {
            add(key, builder.toString());
        }
    }
}
