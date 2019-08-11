package mekanism.common.security;

import mekanism.api.EnumColor;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.LangUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface ISecurityTile {

    TileComponentSecurity getSecurity();

    default boolean hasSecurity() {
        return true;
    }

    enum SecurityMode {
        PUBLIC("mekanism.security.public", EnumColor.BRIGHT_GREEN),
        PRIVATE("mekanism.security.private", EnumColor.RED),
        TRUSTED("mekanism.security.trusted", EnumColor.ORANGE);

        private String display;
        private EnumColor color;

        SecurityMode(String s, EnumColor c) {
            display = s;
            color = c;
        }

        public String getDisplay() {
            return color + LangUtils.localize(display);
        }

        public ITextComponent getTextComponent() {
            return new TranslationTextComponent(display).applyTextStyle(color.textFormatting);
        }
    }
}