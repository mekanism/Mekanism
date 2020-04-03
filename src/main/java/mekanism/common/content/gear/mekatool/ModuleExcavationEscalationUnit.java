package mekanism.common.content.gear.mekatool;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.IDisableableEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ModuleExcavationEscalationUnit extends ModuleMekaTool {

    private ModuleConfigItem<ExcavationMode> excavationMode;

    @Override
    public void init() {
        super.init();
        addConfigItem(excavationMode = new ModuleConfigItem<ExcavationMode>(this, "excavation_mode", MekanismLang.MODULE_MODE, new EnumData<>(ExcavationMode.class), ExcavationMode.NORMAL));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        ExcavationMode newMode = excavationMode.get().adjust(shift);
        if (excavationMode.get() != newMode) {
            excavationMode.set(newMode, null);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.DISASSEMBLER_MODE_CHANGE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, newMode, EnumColor.AQUA, newMode.getEfficiency())));
            }
        }
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list) {
        list.add(MekanismLang.MODE.translate(EnumColor.INDIGO, excavationMode.get()));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translate(EnumColor.INDIGO, excavationMode.get().getEfficiency()));
    }

    public double getEfficiency() {
        return excavationMode.get().getEfficiency();
    }

    public enum ExcavationMode implements IDisableableEnum<ExcavationMode>, IHasTranslationKey, IHasTextComponent {
        NORMAL(MekanismLang.DISASSEMBLER_NORMAL, 20, () -> true),
        FAST(MekanismLang.DISASSEMBLER_FAST, 128, MekanismConfig.general.disassemblerFastMode),
        OFF(MekanismLang.DISASSEMBLER_OFF, 0, () -> true),
        SLOW(MekanismLang.DISASSEMBLER_SLOW, 8, MekanismConfig.general.disassemblerSlowMode);

        private static ExcavationMode[] MODES = values();

        private final BooleanSupplier checkEnabled;
        private final ILangEntry langEntry;
        private final int efficiency;

        ExcavationMode(ILangEntry langEntry, int efficiency, BooleanSupplier checkEnabled) {
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.checkEnabled = checkEnabled;
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
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translate();
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
