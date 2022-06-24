package mekanism.api.text;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {

    private TextComponentUtil() {
    }

    /**
     * Helper to apply an integer color style to a given text component.
     *
     * @param component Component to color.
     * @param color     RGB color to apply.
     *
     * @return Colored component.
     */
    public static MutableComponent color(MutableComponent component, int color) {
        return component.setStyle(component.getStyle()
              .withColor(TextColor.fromRgb(color)));
    }

    /**
     * Builds a formattable text component out of a list of components using a "smart" combination system to allow for automatic replacements, and coloring to take
     * place.
     *
     * @param components Argument components.
     *
     * @return Formattable Text Component.
     */
    public static MutableComponent build(Object... components) {
        //TODO: Verify that just appending them to the first text component works properly.
        // My suspicion is we will need to chain downwards and append it that way so that the formatting matches
        // from call to call without resetting back to
        MutableComponent result = null;
        Style cachedStyle = Style.EMPTY;
        for (Object component : components) {
            if (component == null) {
                //If the component doesn't exist just skip it
                continue;
            }
            MutableComponent current = null;
            if (component instanceof IHasTextComponent hasTextComponent) {
                current = hasTextComponent.getTextComponent().copy();
            } else if (component instanceof IHasTranslationKey hasTranslationKey) {
                current = translate(hasTranslationKey.getTranslationKey());
            } else if (component instanceof EnumColor color) {
                cachedStyle = cachedStyle.withColor(color.getColor());
            } else if (component instanceof Component c) {
                //Just append if a text component is being passed
                current = c.copy();
            } else if (component instanceof ChatFormatting formatting) {
                cachedStyle = cachedStyle.applyFormat(formatting);
            } else if (component instanceof ClickEvent event) {
                cachedStyle = cachedStyle.withClickEvent(event);
            } else if (component instanceof HoverEvent event) {
                cachedStyle = cachedStyle.withHoverEvent(event);
            } else if (component instanceof Block block) {
                current = translate(block.getDescriptionId());
            } else if (component instanceof Item item) {
                current = translate(item.getDescriptionId());
            } else if (component instanceof ItemStack stack) {
                current = stack.getHoverName().copy();
            } else if (component instanceof FluidStack stack) {
                current = stack.getDisplayName().copy();
            } else if (component instanceof Fluid fluid) {
                current = translate(fluid.getFluidType().getDescriptionId());
            } else if (component instanceof Direction direction) {
                current = getTranslatedDirection(direction);
            } else if (component instanceof Boolean bool) {
                current = getTranslatedBoolean(bool);
            } else {
                //Fallback to a generic replacement
                // this handles strings, numbers, and any type we don't necessarily know about
                current = getString(component.toString());
            }
            if (current == null) {
                //If we don't have a component to add, don't
                continue;
            }
            if (!cachedStyle.isEmpty()) {
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
        //TODO: Make this more like smartTranslate? Including back to back formatting where we already have that type of formatting set
        // then convert that
        //Ignores any trailing formatting
        return result;
    }

    private static MutableComponent getTranslatedBoolean(boolean bool) {
        return (bool ? APILang.TRUE_LOWER : APILang.FALSE_LOWER).translate();
    }

    private static MutableComponent getTranslatedDirection(Direction direction) {
        return (switch (direction) {
            case DOWN -> APILang.DOWN;
            case UP -> APILang.UP;
            case NORTH -> APILang.NORTH;
            case SOUTH -> APILang.SOUTH;
            case WEST -> APILang.WEST;
            case EAST -> APILang.EAST;
        }).translate();
    }

    /**
     * Helper to call the constructor for string text components and also convert any non-breaking spaces to spaces so that they render properly.
     *
     * @param component String
     *
     * @return String Text Component.
     */
    public static MutableComponent getString(String component) {
        return Component.literal(cleanString(component));
    }

    /**
     * Helper to clean up strings and convert any non-breaking spaces to spaces so that they render properly.
     *
     * @param component String
     *
     * @return Cleaned string
     */
    private static String cleanString(String component) {
        return component.replace("\u00A0", " ")//non-breaking space
              .replace("\u202f", " ");//narrow non-breaking space
    }

    /**
     * Helper to call the constructor for translation text components in case we end up ever needing to do any extra processing.
     *
     * @param key  Translation Key.
     * @param args Arguments.
     *
     * @return Translation Text Component.
     */
    public static MutableComponent translate(String key, Object... args) {
        return Component.translatable(key, args);
    }

    /**
     * Smarter version of {@link #translate(String, Object...)} that uses a "smart" replacement scheme for parameters to allow for automatic replacements, and coloring to
     * take place.
     *
     * @param key        Translation Key.
     * @param components Argument components.
     *
     * @return Translation Text Component.
     */
    public static MutableComponent smartTranslate(String key, Object... components) {
        if (components.length == 0) {
            //If we don't have any args just short circuit to creating the translation key
            return translate(key);
        }
        List<Object> args = new ArrayList<>();
        Style cachedStyle = Style.EMPTY;
        for (Object component : components) {
            if (component == null) {
                //If the component doesn't exist add it anyway, because we may want to be replacing it
                // with a literal null in the formatted text
                args.add(null);
                cachedStyle = Style.EMPTY;
                continue;
            }
            MutableComponent current = null;
            if (component instanceof IHasTextComponent hasTextComponent) {
                current = hasTextComponent.getTextComponent().copy();
            } else if (component instanceof IHasTranslationKey hasTranslationKey) {
                current = translate(hasTranslationKey.getTranslationKey());
            } else if (component instanceof Block block) {
                current = translate(block.getDescriptionId());
            } else if (component instanceof Item item) {
                current = translate(item.getDescriptionId());
            } else if (component instanceof ItemStack stack) {
                current = stack.getHoverName().copy();
            } else if (component instanceof FluidStack stack) {
                current = stack.getDisplayName().copy();
            } else if (component instanceof Fluid fluid) {
                current = translate(fluid.getFluidType().getDescriptionId());
            } else if (component instanceof Direction direction) {
                current = getTranslatedDirection(direction);
            } else if (component instanceof Boolean bool) {
                current = getTranslatedBoolean(bool);
            }
            //Formatting
            else if (component instanceof EnumColor color && cachedStyle.getColor() == null) {
                //No color set yet in the cached style, apply the color
                cachedStyle = cachedStyle.withColor(color.getColor());
                continue;
            } else if (component instanceof ChatFormatting formatting && !hasStyleType(cachedStyle, formatting)) {
                //Specific formatting not in the cached style yet, apply it
                cachedStyle = cachedStyle.applyFormat(formatting);
                continue;
            } else if (component instanceof ClickEvent event && cachedStyle.getClickEvent() == null) {
                //No click event set yet in the cached style, add the event
                cachedStyle = cachedStyle.withClickEvent(event);
                continue;
            } else if (component instanceof HoverEvent event && cachedStyle.getHoverEvent() == null) {
                //No hover event set yet in the cached style, add the event
                cachedStyle = cachedStyle.withHoverEvent(event);
                continue;
            } else if (!cachedStyle.isEmpty()) {
                //Only bother attempting these checks if we have a cached format, because
                // otherwise we are just going to want to use the raw text
                if (component instanceof Component) {
                    //Just append if a text component is being passed
                    current = ((Component) component).copy();
                } else if (component instanceof EnumColor) {
                    //If we already have a color in our format allow using the EnumColor's name
                    current = ((EnumColor) component).getName();
                } else {
                    //Fallback to a direct replacement just so that we can properly color it
                    // this handles strings, numbers, and any type we don't necessarily know about
                    current = getString(component.toString());
                }
            } else if (component instanceof String) {
                //If we didn't format it, and it is a string make sure we clean it up
                component = cleanString((String) component);
            }
            if (!cachedStyle.isEmpty()) {
                //If we don't have a text component, then we have to just ignore the formatting and
                // add it directly as an argument. (Note: This should never happen because of the fallback)
                if (current == null) {
                    args.add(component);
                } else {
                    //Otherwise, we apply the formatting and then add it
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
        if (!cachedStyle.isEmpty()) {
            //Add trailing formatting as a color name or just directly
            //Note: We know that we have at least one element in the array, so we don't need to safety check here
            Object lastComponent = components[components.length - 1];
            if (lastComponent instanceof EnumColor color) {
                args.add(color.getName());
            } else {
                args.add(lastComponent);
            }
            //TODO: If we have multiple trailing formatting types such as a color and italics, we may want to eventually
            // handle how we add them to the arguments better?
        }
        return translate(key, args.toArray());
    }

    private static boolean hasStyleType(Style current, ChatFormatting formatting) {
        return switch (formatting) {
            case OBFUSCATED -> current.isObfuscated();
            case BOLD -> current.isBold();
            case STRIKETHROUGH -> current.isStrikethrough();
            case UNDERLINE -> current.isUnderlined();
            case ITALIC -> current.isItalic();
            case RESET -> current.isEmpty();
            default -> current.getColor() != null;
        };
    }
}