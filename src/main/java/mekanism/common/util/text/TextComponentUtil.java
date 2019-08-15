package mekanism.common.util.text;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.block.Block;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {

    //TODO: Make some just have Translation using formatting rather than building up a layered ITextComponent
    public static ITextComponent build(Object... components) {
        //TODO: Verify that just appending them to the first text component works properly.
        // My suspicion is we will need to chain downwards and append it that way so that the formatting matches
        // from call to call without resetting back to
        ITextComponent result = null;
        TextFormatting cachedFormat = null;
        for (Object component : components) {
            if (component == null) {
                //If the component doesn't exist just skip it
                continue;
            }
            ITextComponent current = null;
            if (component instanceof IHasTextComponent) {
                current = ((IHasTextComponent) component).getTextComponent();
            } else if (component instanceof IHasTranslationKey) {
                current = translate(((IHasTranslationKey) component).getTranslationKey());
            } else if (component instanceof String) {
                current = getString((String) component);
            } else if (component instanceof EnumColor) {
                cachedFormat = ((EnumColor) component).textFormatting;
            } else if (component instanceof ITextComponent) {
                //Just append if a text component is being passed
                current = (ITextComponent) component;
            } else if (component instanceof TextFormatting) {
                cachedFormat = (TextFormatting) component;
            } else if (component instanceof InputMappings.Input) {
                //Helper for key bindings to not have to get the translation key directly and then pass that as a Translation object
                current = translate(((InputMappings.Input) component).getTranslationKey());
            } else if (component instanceof Block) {
                current = ((Block) component).getNameTextComponent();
            } else if (component instanceof Item) {
                current = translate(((Item) component).getTranslationKey());
            } else if (component instanceof FluidStack) {
                current = translate(((FluidStack) component).getUnlocalizedName());
            } else if (component instanceof Fluid) {
                current = translate(((Fluid) component).getUnlocalizedName());
            } else if (component instanceof Boolean || component instanceof Number) {
                //Put actual boolean or integer/double, etc value
                current = getString(component.toString());
            } else {
                //TODO: Warning when unexpected type?
                //TODO: Add support wrappers for following types
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

    //TODO: Rename this to getString
    public static StringTextComponent getString(String component) {
        return new StringTextComponent(component);
    }

    //TODO: Rename this to getTranslation or translate
    public static TranslationTextComponent translate(String component, Object... args) {
        return new TranslationTextComponent(component, args);
    }
}