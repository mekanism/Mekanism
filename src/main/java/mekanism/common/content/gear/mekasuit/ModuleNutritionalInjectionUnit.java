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
        if (MekanismUtils.isPlayingMode(player) && player.canEat(false) && module.getContainerEnergy().greaterOrEqual(usage)) {
            ItemStack container = module.getContainer();
            ItemMekaSuitArmor item = (ItemMekaSuitArmor) container.getItem();
            long toFeed = Math.min(1, item.getContainedGas(container, MekanismGases.NUTRITIONAL_PASTE.get()).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            if (toFeed > 0) {
                module.useEnergy(player, usage.multiply(toFeed));
                item.useGas(container, MekanismGases.NUTRITIONAL_PASTE.get(), toFeed * MekanismConfig.general.nutritionalPasteMBPerFood.get());
                //TODO - 10.1: Re-evaluate the calculations related to this as it seems slightly different than the canteen
                // and I think the 1, should be toFeed except I am not sure toFeed can ever by > 1 currently
                player.getFoodData().eat(1, MekanismConfig.general.nutritionalPasteSaturation.get());
            }
        }
    }

    @Override
    public void addHUDElements(IModule<ModuleNutritionalInjectionUnit> module, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            ItemStack container = module.getContainer();
            GasStack stored = ((ItemMekaSuitArmor) container.getItem()).getContainedGas(container, MekanismGases.NUTRITIONAL_PASTE.get());
            double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitNutritionalMaxStorage.getAsLong());
            hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElementPercent(icon, ratio));
        }
    }
}