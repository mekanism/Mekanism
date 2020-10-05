package mekanism.api.text;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {

    private TextComponentUtil() {
    }

    public static IFormattableTextComponent getFormattableComponent(ITextComponent component) {
        return component instanceof IFormattableTextComponent ? (IFormattableTextComponent) component : component.deepCopy();
    }

    public static IFormattableTextComponent build(Object... components) {
        //TODO: Verify that just appending them to the first text component works properly.
        // My suspicion is we will need to chain downwards and append it that way so that the formatting matches
        // from call to call without resetting back to
        IFormattableTextComponent result = null;
        Style cachedStyle = Style.EMPTY;
        for (Object component : components) {
            if (component == null) {
                //If the component doesn't exist just skip it
                continue;
            }
            IFormattableTextComponent current = null;
            if (component instanceof IHasTextComponent) {
                current = getFormattableComponent(((IHasTextComponent) component).getTextComponent());
            } else if (component instanceof IHasTranslationKey) {
                current = translate(((IHasTranslationKey) component).getTranslationKey());
            } else if (component instanceof EnumColor) {
                cachedStyle = cachedStyle.setColor(((EnumColor) component).getColor());
            } else if (component instanceof ITextComponent) {
                //Just append if a text component is being passed
                current = getFormattableComponent((ITextComponent) component);
            } else if (component instanceof TextFormatting) {
                cachedStyle = cachedStyle.applyFormatting((TextFormatting) component);
            } else if (component instanceof Block) {
                current = translate(((Block) component).getTranslationKey());
            } else if (component instanceof Item) {
                current = translate(((Item) component).getTranslationKey());
            } else if (component instanceof ItemStack) {
                current = getFormattableComponent(((ItemStack) component).getDisplayName());
            } else if (component instanceof FluidStack) {
                current = translate(((FluidStack) component).getTranslationKey());
            } else if (component instanceof Fluid) {
                current = translate(((Fluid) component).getAttributes().getTranslationKey());
            } else if (component instanceof Direction) {
                current = getTranslatedDirection((Direction) component);
            } else if (component instanceof String || component instanceof Boolean || component instanceof Number) {
                //Put actual boolean or integer/double, etc value
                current = getString(component.toString());
            } else {
                //Fallback to a generic replacement
                current = APILang.GENERIC.translate(component);
            }
            if (current == null) {
                //If we don't have a component to add, don't
                continue;
            }
            if (!cachedStyle.equals(Style.EMPTY)) {
                //Apply the style and reset
                current.setStyle(cachedStyle);
                cachedStyle = Style.EMPTY;
            }
            if (result == null) {
                result = current;
            } else {
                result.append(current);
            }
        }
        //TODO: Make this more like smartTranslate? Including back to back color codes treat the second as name?
        //Ignores any trailing formatting
        return result;
    }

    private static IFormattableTextComponent getTranslatedDirection(Direction direction) {
        switch (direction) {
            case DOWN:
                return APILang.DOWN.translate();
            case UP:
                return APILang.UP.translate();
            case NORTH:
                return APILang.NORTH.translate();
            case SOUTH:
                return APILang.SOUTH.translate();
            case WEST:
                return APILang.WEST.translate();
            case EAST:
                return APILang.EAST.translate();
        }
        return getString(direction.toString());
    }

    public static StringTextComponent getString(String component) {
        return new StringTextComponent(cleanString(component));
    }

    private static String cleanString(String component) {
        return component.replace("\u00A0", " ");
    }

    public static TranslationTextComponent translate(String key, Object... args) {
        return new TranslationTextComponent(key, args);
    }

    public static TranslationTextComponent smartTranslate(String key, Object... components) {
        if (components.length == 0) {
            //If we don't have any args just short circuit to creating the translation key
            return translate(key);
        }
        List<Object> args = new ArrayList<>();
        Style cachedStyle = Style.EMPTY;
        for (Object component : components) {
            if (component == null) {
                //If the component doesn't exist add it anyways, because we may want to be replacing it
                // with a literal null in the formatted text
                args.add(null);
                cachedStyle = Style.EMPTY;
                continue;
            }
            IFormattableTextComponent current = null;
            if (component instanceof IHasTextComponent) {
                current = getFormattableComponent(((IHasTextComponent) component).getTextComponent());
            } else if (component instanceof IHasTranslationKey) {
                current = translate(((IHasTranslationKey) component).getTranslationKey());
            } else if (component instanceof Block) {
                current = translate(((Block) component).getTranslationKey());
            } else if (component instanceof Item) {
                current = translate(((Item) component).getTranslationKey());
            } else if (component instanceof ItemStack) {
                current = getFormattableComponent(((ItemStack) component).getDisplayName());
            } else if (component instanceof FluidStack) {
                current = translate(((FluidStack) component).getTranslationKey());
            } else if (component instanceof Fluid) {
                current = translate(((Fluid) component).getAttributes().getTranslationKey());
            } else if (component instanceof Direction) {
                current = getTranslatedDirection((Direction) component);
            } else if (!cachedStyle.equals(Style.EMPTY)) {
                //Only bother attempting these checks if we have a cached format, because
                // otherwise we are just going to want to use the raw text
                if (component instanceof ITextComponent) {
                    //Just append if a text component is being passed
                    current = getFormattableComponent((ITextComponent) component);
                } else if (component instanceof String || component instanceof Boolean || component instanceof Number) {
                    //Put actual boolean or integer/double, etc value
                    current = getString(component.toString());
                } else if (component instanceof EnumColor) {
                    //If we already have a format allow using the EnumColor's name
                    current = ((EnumColor) component).getName();
                } else {
                    //Fallback to a direct replacement just so that we can properly color it
                    current = APILang.GENERIC.translate(component);
                }
            } else if (component instanceof String) {
                //If we didn't format it and it is a string make sure we clean it up
                component = cleanString((String) component);
            }
            //Formatting
            else if (component instanceof EnumColor) {
                cachedStyle = cachedStyle.setColor(((EnumColor) component).getColor());
                continue;
            } else if (component instanceof TextFormatting) {
                cachedStyle = cachedStyle.applyFormatting((TextFormatting) component);
                continue;
            }
            if (!cachedStyle.equals(Style.EMPTY)) {
                //If we don't have a text component, then we have to just ignore the formatting and
                // add it directly as an argument. (Note: This should never happen because of the fallback)
                if (current == null) {
                    args.add(component);
                } else {
                    //Otherwise we apply the formatting and then add it
                    args.add(current.setStyle(cachedStyle));
                }
                cachedStyle = Style.EMPTY;
            } else if (current == null) {
                //Add raw
                args.add(component);
            } else {
                //Add the text component variant of it
                args.add(current);
            }
        }
        if (!cachedStyle.equals(Style.EMPTY)) {
            //Add trailing formatting as a color name or just directly
            //Note: We know that we have at least one element in the array, so we don't need to safety check here
            Object lastComponent = components[components.length - 1];
            if (lastComponent instanceof EnumColor) {
                args.add(((EnumColor) lastComponent).getName());
            } else {
                args.add(lastComponent);
            }
        }
        return translate(key, args.toArray());
    }
}