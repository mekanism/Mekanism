package mekanism.common.security;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
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

    enum SecurityMode implements IIncrementalEnum<SecurityMode>, IHasTextComponent {
        PUBLIC("security.mekanism.public", EnumColor.BRIGHT_GREEN),
        PRIVATE("security.mekanism.private", EnumColor.RED),
        TRUSTED("security.mekanism.trusted", EnumColor.ORANGE);

        private static final SecurityMode[] MODES = values();
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

        @Nonnull
        @Override
        public SecurityMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static SecurityMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}