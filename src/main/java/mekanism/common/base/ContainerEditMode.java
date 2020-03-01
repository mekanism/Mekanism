package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public enum ContainerEditMode implements IIncrementalEnum<ContainerEditMode>, IHasTextComponent {
    BOTH(MekanismLang.FLUID_CONTAINER_BOTH),
    FILL(MekanismLang.FLUID_CONTAINER_FILL),
    EMPTY(MekanismLang.FLUID_CONTAINER_EMPTY);

    private static final ContainerEditMode[] MODES = values();
    private final ILangEntry langEntry;

    ContainerEditMode(ILangEntry langEntry) {
        this.langEntry = langEntry;
    }

    @Override
    public ITextComponent getTextComponent() {
        return langEntry.translate();
    }

    @Nonnull
    @Override
    public ContainerEditMode byIndex(int index) {
        return byIndexStatic(index);
    }

    public static ContainerEditMode byIndexStatic(int index) {
        //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
        return MODES[Math.floorMod(index, MODES.length)];
    }
}