package mekanism.common.world.height;

import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.jetbrains.annotations.NotNull;

public enum AnchorType implements IHasEnumNameTranslationKey {
    ABSOLUTE(MekanismLang.ANCHOR_TYPE_ABSOLUTE, (context, value) -> value),
    ABOVE_BOTTOM(MekanismLang.ANCHOR_TYPE_ABOVE_BOTTOM, (context, value) -> context.getMinGenY() + value),
    BELOW_TOP(MekanismLang.ANCHOR_TYPE_BELOW_TOP, (context, value) -> context.getGenDepth() - 1 + context.getMinGenY() - value);

    private final ILangEntry langEntry;
    private final YResolver yResolver;

    AnchorType(ILangEntry langEntry, YResolver yResolver) {
        this.langEntry = langEntry;
        this.yResolver = yResolver;
    }

    public int resolveY(WorldGenerationContext context, int value) {
        return yResolver.resolve(context, value);
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @FunctionalInterface
    private interface YResolver {

        int resolve(WorldGenerationContext context, int value);
    }
}