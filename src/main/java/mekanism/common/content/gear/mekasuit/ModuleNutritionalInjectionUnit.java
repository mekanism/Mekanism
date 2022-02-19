package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
public class ModuleNutritionalInjectionUnit implements ICustomModule<ModuleNutritionalInjectionUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "nutritional_injection_unit.png");

    @Override
    public void tickServer(IModule<ModuleNutritionalInjectionUnit> module, PlayerEntity player) {
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
        if (MekanismUtils.isPlayingMode(player) && player.canEat(false)) {
            //Check if we can use a single iteration of it
            ItemStack container = module.getContainer();
            ItemMekaSuitArmor item = (ItemMekaSuitArmor) container.getItem();
            long needed = Math.min(20 - player.getFoodData().getFoodLevel(),
                  item.getContainedGas(container, MekanismGases.NUTRITIONAL_PASTE.get()).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            long toFeed = Math.min(module.getContainerEnergy().divideToInt(usage), needed);
            if (toFeed > 0) {
                module.useEnergy(player, usage.multiply(toFeed));
                item.useGas(container, MekanismGases.NUTRITIONAL_PASTE.get(), toFeed * MekanismConfig.general.nutritionalPasteMBPerFood.get());
                player.getFoodData().eat((int) needed, needed * MekanismConfig.general.nutritionalPasteSaturation.get());
            }
        }
    }

    @Override
    public void addHUDElements(IModule<ModuleNutritionalInjectionUnit> module, PlayerEntity player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            ItemStack container = module.getContainer();
            GasStack stored = ((ItemMekaSuitArmor) container.getItem()).getContainedGas(container, MekanismGases.NUTRITIONAL_PASTE.get());
            double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitNutritionalMaxStorage.getAsLong());
            hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElementPercent(icon, ratio));
        }
    }
}