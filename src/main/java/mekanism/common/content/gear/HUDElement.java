package mekanism.common.content.gear;

import java.util.function.IntSupplier;
import mekanism.api.gear.IHUDElement;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HUDElement implements IHUDElement {

    private final ResourceLocation icon;
    private final Component text;
    private final HUDColor color;

    private HUDElement(ResourceLocation icon, Component text, HUDColor color) {
        this.icon = icon;
        this.text = text;
        this.color = color;
    }

    @NotNull
    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public Component getText() {
        return text;
    }

    @Override
    public int getColor() {
        return color.getColorARGB();
    }

    public static HUDElement of(ResourceLocation icon, Component text, HUDColor color) {
        return new HUDElement(icon, text, color);
    }

    public enum HUDColor {
        REGULAR(MekanismConfig.client.hudColor),
        FADED(() -> REGULAR.getColor().darken(0.5).rgb()),
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
            return switch (apiColor) {
                case REGULAR -> REGULAR;
                case FADED -> FADED;
                case WARNING -> WARNING;
                case DANGER -> DANGER;
            };
        }
    }
}