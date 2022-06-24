package mekanism.common.tile.interfaces;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

public interface IFluidContainerManager extends IHasMode {

    ContainerEditMode getContainerEditMode();

    @NothingNullByDefault
    enum ContainerEditMode implements IIncrementalEnum<ContainerEditMode>, IHasTextComponent {
        BOTH(MekanismLang.FLUID_CONTAINER_BOTH),
        FILL(MekanismLang.FLUID_CONTAINER_FILL),
        EMPTY(MekanismLang.FLUID_CONTAINER_EMPTY);

        private static final ContainerEditMode[] MODES = values();
        private final ILangEntry langEntry;

        ContainerEditMode(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        @Override
        public ContainerEditMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ContainerEditMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}