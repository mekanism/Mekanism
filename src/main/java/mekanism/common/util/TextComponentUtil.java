package mekanism.common.util;

import mekanism.api.EnumColor;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TextComponentUtil {

    //TODO: Support String formatting replacements for TranslationTextComponent
    public static ITextComponent build(Object... components) {
        //TODO: Verify that just appending them to the first text component works properly.
        // My suspicion is we will need to chain downwards and append it that way so that the formatting matches
        // from call to call without resetting back to
        ITextComponent result = null;
        TextFormatting cachedFormat = null;
        for (Object component : components) {
            ITextComponent current = null;
            if (component instanceof Translation) {
                current = new TranslationTextComponent(((Translation) component).key);
            } else if (component instanceof String) {
                current = new StringTextComponent((String) component);
            } else if (component instanceof InputMappings.Input) {
                //Helper for key bindings to not have to get the translation key directly and then pass that as a Translation object
                current = new TranslationTextComponent(((InputMappings.Input) component).getTranslationKey());
            } else if (component instanceof EnumColor) {
                cachedFormat = ((EnumColor) component).textFormatting;
            } else if (component instanceof TextFormatting) {
                cachedFormat = (TextFormatting) component;
            } else {
                //TODO: Warning when unexpected type?
            }
            if (current == null) {
                //If we don't have a component to add, don't
                continue;
            }
            if (cachedFormat != null) {
                //Apply the formatting
                current.applyTextStyle(cachedFormat);
                cachedFormat = null;
            }
            if (result == null) {
                result = current;
            } else {
                result.appendSibling(current);
            }
        }
        //Ignores any trailing formatting
        return result;
    }

    public static class Translation {

        private final String key;

        private Translation(String key) {
            this.key = key;
        }

        public static Translation of(String key) {
            return new Translation(key);
        }
    }
}