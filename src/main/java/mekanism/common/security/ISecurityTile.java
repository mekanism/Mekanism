package mekanism.common.security;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.text.ITextComponent;

public interface ISecurityTile {

    TileComponentSecurity getSecurity();

    default boolean hasSecurity() {
        return true;
    }

    enum SecurityMode implements IHasTextComponent {
        PUBLIC("security.mekanism.public", EnumColor.BRIGHT_GREEN),
        PRIVATE("security.mekanism.private", EnumColor.RED),
        TRUSTED("security.mekanism.trusted", EnumColor.ORANGE);

        private String display;
        private EnumColor color;

        SecurityMode(String s, EnumColor c) {
            display = s;
            color = c;
        }

        @Override
        public ITextComponent getTextComponent() {
            return TextComponentUtil.build(color, Translation.of(display));
        }
    }
}