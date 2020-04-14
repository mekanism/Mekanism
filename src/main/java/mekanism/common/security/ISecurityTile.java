package mekanism.common.security;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.tile.component.TileComponentSecurity;
import net.minecraft.util.text.ITextComponent;

public interface ISecurityTile {

    TileComponentSecurity getSecurity();

    default boolean hasSecurity() {
        return true;
    }

    enum SecurityMode implements IIncrementalEnum<SecurityMode>, IHasTextComponent {
        PUBLIC(MekanismLang.PUBLIC, EnumColor.BRIGHT_GREEN),
        PRIVATE(MekanismLang.PRIVATE, EnumColor.RED),
        TRUSTED(MekanismLang.TRUSTED, EnumColor.ORANGE);

        private static final SecurityMode[] MODES = values();

        private final ILangEntry langEntry;
        private final EnumColor color;

        SecurityMode(ILangEntry langEntry, EnumColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public SecurityMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static SecurityMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}