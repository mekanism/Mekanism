package mekanism.common.content.gear.mekatool;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.IDisableableEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
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

    public enum ExcavationMode implements IDisableableEnum<ExcavationMode>, IHasTextComponent {
        OFF(0, () -> true),
        SLOW(4, MekanismConfig.gear.disassemblerSlowMode),
        NORMAL(16, () -> true),
        FAST(32, () -> true),
        SUPERFAST(64, () -> true),
        EXTREME(128, MekanismConfig.gear.disassemblerFastMode);

        private static ExcavationMode[] MODES = values();

        private final BooleanSupplier checkEnabled;
        private final ITextComponent label;
        private final int efficiency;

        ExcavationMode(int efficiency, BooleanSupplier checkEnabled) {
            this.efficiency = efficiency;
            this.checkEnabled = checkEnabled;
            this.label = new StringTextComponent(Integer.toString(efficiency));
        }

        /**
         * Gets a Mode from its ordinal. NOTE: if this mode is not enabled then it will reset to NORMAL
         */
        public static ExcavationMode byIndexStatic(int index) {
            ExcavationMode mode = MODES[Math.floorMod(index, MODES.length)];
            return mode.isEnabled() ? mode : NORMAL;
        }

        @Nonnull
        @Override
        public ExcavationMode byIndex(int index) {
            return MODES[Math.floorMod(index, MODES.length)];
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getEfficiency() {
            return efficiency;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.getAsBoolean();
        }
    }
}
