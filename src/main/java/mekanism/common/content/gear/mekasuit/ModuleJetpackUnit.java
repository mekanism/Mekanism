package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
public class ModuleJetpackUnit implements ICustomModule<ModuleJetpackUnit> {

    private IModuleConfigItem<JetpackMode> jetpackMode;

    @Override
    public void init(IModule<ModuleJetpackUnit> module, ModuleConfigItemCreator configItemCreator) {
        jetpackMode = configItemCreator.createConfigItem("jetpack_mode", MekanismLang.MODULE_JETPACK_MODE, new ModuleEnumData<>(JetpackMode.class, JetpackMode.NORMAL));
    }

    @Override
    public void addHUDElements(IModule<ModuleJetpackUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            ItemStack container = module.getContainer();
            GasStack stored = ((ItemMekaSuitArmor) container.getItem()).getContainedGas(container, MekanismGases.HYDROGEN.get());
            double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitJetpackMaxStorage.getAsLong());
            hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElementPercent(jetpackMode.get().getHUDIcon(), ratio));
        }
    }

    @Override
    public void changeMode(IModule<ModuleJetpackUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        if (module.isEnabled()) {
            JetpackMode newMode = jetpackMode.get().adjust(shift);
            if (jetpackMode.get() != newMode) {
                jetpackMode.set(newMode);
                if (displayChangeMessage) {
                    module.displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(), newMode);
                }
            }
        }
    }

    public JetpackMode getMode() {
        return jetpackMode.get();
    }
}
