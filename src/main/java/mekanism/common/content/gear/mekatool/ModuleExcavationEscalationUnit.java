package mekanism.common.content.gear.mekatool;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ModuleExcavationEscalationUnit extends ModuleMekaTool {

    private ModuleConfigItem<ExcavationMode> excavationMode;

    @Override
    public void init() {
        super.init();
        addConfigItem(excavationMode = new ModuleConfigItem<>(this, "excavation_mode", MekanismLang.MODULE_EFFICIENCY, new EnumData<>(ExcavationMode.class, getInstalledCount() + 2), ExcavationMode.NORMAL));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (!isEnabled()) {
            return;
        }
        ExcavationMode newMode = excavationMode.get().adjust(shift);
        if (excavationMode.get() != newMode) {
            excavationMode.set(newMode, null);
            if (displayChangeMessage) {
                displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
            }
        }
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list) {
        if (!isEnabled()) {
            return;
        }
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, excavationMode.get().getEfficiency()));
    }

    public double getEfficiency() {
        return excavationMode.get().getEfficiency();
    }

    public enum ExcavationMode implements IIncrementalEnum<ExcavationMode>, IHasTextComponent {
        OFF(0),
        SLOW(4),
        NORMAL(16),
        FAST(32),
        SUPER_FAST(64),
        EXTREME(128);

        private static final ExcavationMode[] MODES = values();

        private final ITextComponent label;
        private final int efficiency;

        ExcavationMode(int efficiency) {
            this.efficiency = efficiency;
            this.label = new StringTextComponent(Integer.toString(efficiency));
        }

        @Nonnull
        @Override
        public ExcavationMode byIndex(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getEfficiency() {
            return efficiency;
        }
    }
}
