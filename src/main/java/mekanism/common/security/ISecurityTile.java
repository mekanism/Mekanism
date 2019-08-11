package mekanism.common.security;

import mekanism.api.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.LangUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.text.ITextComponent;

public interface ISecurityTile {

    TileComponentSecurity getSecurity();

    default boolean hasSecurity() {
        return true;
    }

    enum SecurityMode implements IHasTextComponent {
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

        @Override
        public ITextComponent getTextComponent() {
            return TextComponentUtil.build(color, display);
        }
    }
}