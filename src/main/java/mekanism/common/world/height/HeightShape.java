package mekanism.common.world.height;

import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import org.jetbrains.annotations.NotNull;

public enum HeightShape implements IHasEnumNameTranslationKey {
    TRAPEZOID(MekanismLang.HEIGHT_SHAPE_TRAPEZOID),
    UNIFORM(MekanismLang.HEIGHT_SHAPE_UNIFORM);

    private final ILangEntry langEntry;

    HeightShape(ILangEntry langEntry) {
        this.langEntry = langEntry;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }
}