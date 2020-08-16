package mekanism.common.content.gear.mekasuit;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ModuleJetpackUnit extends ModuleMekaSuit {

    private ModuleConfigItem<JetpackMode> jetpackMode;

    @Override
    public void init() {
        super.init();
        addConfigItem(jetpackMode = new ModuleConfigItem<>(this, "jetpack_mode", MekanismLang.MODULE_JETPACK_MODE, new EnumData<>(JetpackMode.class), JetpackMode.NORMAL));
    }

    @Override
    public void addHUDElements(List<HUDElement> list) {
        if (!isEnabled()) {
            return;
        }
        GasStack stored = ((ItemMekaSuitArmor) getContainer().getItem()).getContainedGas(getContainer(), MekanismGases.HYDROGEN.get());
        double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitJetpackMaxStorage.getAsLong());
        list.add(HUDElement.percent(jetpackMode.get().getHUDIcon(), ratio));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (!isEnabled()) {
            return;
        }
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
