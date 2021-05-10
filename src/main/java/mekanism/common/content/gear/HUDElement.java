package mekanism.common.content.gear;

import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.api.gear.IHUDElement;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class HUDElement implements IHUDElement {

    private final ResourceLocation icon;
    private final ITextComponent text;
    private final HUDColor color;

    private HUDElement(ResourceLocation icon, ITextComponent text, HUDColor color) {
        this.icon = icon;
        this.text = text;
        this.color = color;
    }

    @Nonnull
    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Nonnull
    @Override
    public ITextComponent getText() {
        return text;
    }

    @Override
    public int getColor() {
        return color.getColorARGB();
    }

    public static HUDElement of(ResourceLocation icon, ITextComponent text, HUDColor color) {
        return new HUDElement(icon, text, color);
    }

    public enum HUDColor {
        REGULAR(MekanismConfig.client.hudColor),
        FADED(() -> REGULAR.getColor().darken(0.5).argb()),
        WARNING(MekanismConfig.client.hudWarningColor),
        DANGER(MekanismConfig.client.hudDangerColor);

        private final IntSupplier color;

        HUDColor(IntSupplier color) {
            this.color = color;
        }

        public Color getColor() {
            return Color.rgb(color.getAsInt()).alpha(MekanismConfig.client.hudOpacity.get());
        }

        public int getColorARGB() {
            return getColor().argb();
        }

        public static HUDColor from(IHUDElement.HUDColor apiColor) {
            switch (apiColor) {
                default:
                case REGULAR:
                    return REGULAR;
                case FADED:
                    return FADED;
                case WARNING:
                    return WARNING;
                case DANGER:
                    return DANGER;
            }
        }
    }
}