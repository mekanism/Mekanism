package mekanism.common.content.gear.mekasuit;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismGases;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ModuleJetpackUnit extends ModuleMekaSuit {

    private ModuleConfigItem<JetpackMode> jetpackMode;

    @Override
    public void init() {
        super.init();
        addConfigItem(jetpackMode = new ModuleConfigItem<JetpackMode>(this, "jetpack_mode", MekanismLang.MODULE_JETPACK_MODE, new EnumData<>(JetpackMode.class).withScale(0.6F), JetpackMode.NORMAL));
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list) {
        if (!isEnabled()) return;
        list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpackMode.get()));
        GasStack stored = ((ItemMekaSuitArmor) getContainer().getItem()).getContainedGas(getContainer(), MekanismGases.HYDROGEN.get());
        list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored.getAmount()));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (!isEnabled()) return;
        JetpackMode newMode = jetpackMode.get().adjust(shift);
        if (jetpackMode.get() != newMode) {
            jetpackMode.set(newMode, null);
            if (displayChangeMessage) {
                displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(), newMode);
            }
        }
    }

    public JetpackMode getMode() {
        return jetpackMode.get();
    }
}
