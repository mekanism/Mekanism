package mekanism.client.lang;

import java.util.List;
import mekanism.client.lang.FormatSplitter.Component;
import net.minecraft.data.DataGenerator;

public class UpsideDownLanguageProvider extends ConvertibleLanguageProvider {

    private static final String normal = "abcdefghijklmnopqrstuvwxyz" +
                                         "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                         "0123456789" +
                                         ",.?!;\"'`()[]{}<>&_^";
    private static final char[] upside_down = ("\u0250q\u0254p\u01dd\u025f\u0183\u0265\u1d09\u027e\u029e\ua781\u026fuodb\u0279s\u0287n\u028c\u028dx\u028ez" +
                                               "\u2c6f\u15fa\u0186\u15e1\u018e\u2132\u2141HI\u0550\ua7b0\ua780WNO\u0500\ua779\u1d1aS\u27d8\u2229\u039bMX\u2144Z" +
                                               "0\u295d\u1614\u0190\u07c8\u03db9\u312586" +
                                               "'\u02d9\u00bf\u00a1\u061b\u201e,,)(][}{><\u214b\u203ev").toCharArray();

    private static char flip(char c) {
        int index = normal.indexOf(c);
        return index == -1 ? c : upside_down[index];
    }

    private static String convertFormattingCode(String formattingCode, int curIndex, int numArguments) {
        String ending;
        int storedIndex = curIndex;
        //A formatting code can have at most one $ and if it has one then it is the first "argument" after the %
        String[] split = formattingCode.split("\\$");
        if (split.length == 2) {
            //It already has an index, so read that as the stored index
            ending = split[1];
            storedIndex = Integer.parseInt(split[0].substring(1));
        } else {
            //No index stored in the formatting code
            ending = formattingCode.substring(1);
        }
        //Compare the index the argument currently has with the index it will have afterwards
        // If they are the same we don't need to include the index argument
        if (storedIndex == numArguments - curIndex + 1) {
            return "%" + ending;
        }
        return "%" + storedIndex + "$" + ending;
    }

    public UpsideDownLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_ud");
        //Note: This technically is supposed to be upside down british english but we are doing it as upside down US english
    }

    //TODO: If at some point we make unit tests, we should add some tests for this and for FormatSplitter
    @Override
    public void convert(String key, List<Component> splitEnglish) {
        int numArguments = (int) splitEnglish.stream().filter(Component::isFormattingCode).count();
        StringBuilder converted = new StringBuilder();
        int curIndex = numArguments;
        for (int i = splitEnglish.size() - 1; i >= 0; i--) {
            Component component = splitEnglish.get(i);
            if (component.isFormattingCode()) {
                //Insert the full code directly
                converted.append(convertFormattingCode(component.getContents(), curIndex--, numArguments));
            } else {
                //Convert each character to being upside down and then insert at end
                char[] toConvert = component.getContents().toCharArray();
                for (int j = toConvert.length - 1; j >= 0; j--) {
                    converted.append(flip(toConvert[j]));
                }
            }
        }
        add(key, new String(converted));
    }
}