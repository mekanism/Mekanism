package mekanism.common.content.gear.mekatool;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

@ParametersAreNonnullByDefault
public class ModuleExcavationEscalationUnit implements ICustomModule<ModuleExcavationEscalationUnit> {

    private IModuleConfigItem<ExcavationMode> excavationMode;

    @Override
    public void init(IModule<ModuleExcavationEscalationUnit> module, ModuleConfigItemCreator configItemCreator) {
        excavationMode = configItemCreator.createConfigItem("excavation_mode", MekanismLang.MODULE_EFFICIENCY,
              new ModuleEnumData<>(ExcavationMode.class, module.getInstalledCount() + 2, ExcavationMode.NORMAL));
    }

    @Override
    public void changeMode(IModule<ModuleExcavationEscalationUnit> module, PlayerEntity player, ItemStack stack, int shift, boolean displayChangeMessage) {
        if (module.isEnabled()) {
            ExcavationMode newMode = excavationMode.get().adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 2);
            if (excavationMode.get() != newMode) {
                excavationMode.set(newMode);
                if (displayChangeMessage) {
                    module.displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
                }
            }
        }
    }

    @Override
    public void addHUDStrings(IModule<ModuleExcavationEscalationUnit> module, PlayerEntity player, Consumer<ITextComponent> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, excavationMode.get().getEfficiency()));
        }
    }

    public float getEfficiency() {
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
            this.label = TextComponentUtil.getString(Integer.toString(efficiency));
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
