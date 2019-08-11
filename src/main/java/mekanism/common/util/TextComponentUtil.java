package mekanism.common.util;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.Upgrade;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

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
                current = getTranslationComponent((Translation) component);
            } else if (component instanceof String) {
                current = getStringComponent((String) component);
            } else if (component instanceof EnumColor) {
                cachedFormat = ((EnumColor) component).textFormatting;
            } else if (component instanceof ITextComponent) {
                //Just append if a text component is being passed
                current = (ITextComponent) component;
            } else if (component instanceof TextFormatting) {
                cachedFormat = (TextFormatting) component;
            } else if (component instanceof InputMappings.Input) {
                //Helper for key bindings to not have to get the translation key directly and then pass that as a Translation object
                current = getTranslationComponent(((InputMappings.Input) component).getTranslationKey());
            } else if (component instanceof EnergyDisplay) {
                current = ((EnergyDisplay) component).getTextComponent();
            } else if (component instanceof Upgrade) {
                current = getStringComponent(((Upgrade) component).getName());
            } else if (component instanceof GasStack) {
                current = getTranslationComponent(((GasStack) component).getGas());
            } else if (component instanceof Gas) {
                current = getTranslationComponent((Gas) component);
            } else if (component instanceof FluidStack) {
                current = getTranslationComponent(((FluidStack) component).getUnlocalizedName());
            } else if (component instanceof Boolean || component instanceof Number) {
                //Put actual boolean or integer/double, etc value
                current = getStringComponent(component.toString());
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

    public static StringTextComponent getStringComponent(String component) {
        return new StringTextComponent(component);
    }

    public static TranslationTextComponent getTranslationComponent(Translation component) {
        return getTranslationComponent(component.key);
    }

    public static TranslationTextComponent getTranslationComponent(String component) {
        return new TranslationTextComponent(component);
    }

    public static TranslationTextComponent getTranslationComponent(Gas gas) {
        return new TranslationTextComponent(gas.getTranslationKey());
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

    public static class EnergyDisplay {

        private final double energy;
        private final double max;

        private EnergyDisplay(double energy, double max) {
            this.energy = energy;
            this.max = max;
        }

        //TODO: Wrapper for getting this from itemstack
        public static EnergyDisplay of(double energy, double max) {
            return new EnergyDisplay(energy, max);
        }

        public ITextComponent getTextComponent() {
            if (energy == Double.MAX_VALUE) {
                return getTranslationComponent("mekanism.gui.infinite");
            }
            return getStringComponent(MekanismUtils.getEnergyDisplay(energy)).appendText("/").appendSibling(getStringComponent(MekanismUtils.getEnergyDisplay(max)));
        }
    }
}