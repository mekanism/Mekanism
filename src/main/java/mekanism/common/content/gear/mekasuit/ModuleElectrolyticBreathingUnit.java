package mekanism.common.content.gear.mekasuit;

import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class ModuleElectrolyticBreathingUnit implements ICustomModule<ModuleElectrolyticBreathingUnit> {

    private IModuleConfigItem<Boolean> fillHeld;

    @Override
    public void init(IModule<ModuleElectrolyticBreathingUnit> module, ModuleConfigItemCreator configItemCreator) {
        fillHeld = configItemCreator.createConfigItem("fill_held", MekanismLang.MODULE_BREATHING_HELD, new ModuleBooleanData());
    }

    @Override
    public void tickServer(IModule<ModuleElectrolyticBreathingUnit> module, PlayerEntity player) {
        int productionRate = 0;
        //Check if the mask is under water
        //Note: Being in water is checked first to ensure that if it is raining and the player is in water
        // they get the full strength production
        double maskHeight = player.getEyeY() - 0.15;
        BlockPos headPos = new BlockPos(player.getX(), maskHeight, player.getZ());
        FluidState fluidstate = player.getCommandSenderWorld().getFluidState(headPos);
        if (fluidstate.is(FluidTags.WATER) && maskHeight <= headPos.getY() + fluidstate.getHeight(player.getCommandSenderWorld(), headPos)) {
            //If the position the bottom of the mask is in is water set the production rate to our max rate
            productionRate = getMaxRate(module);
        } else if (player.isInRain()) {
            //If the player is not in water but is in rain set the production at half power
            productionRate = getMaxRate(module) / 2;
        }
        if (productionRate > 0) {
            FloatingLong usage = MekanismConfig.general.FROM_H2.get().multiply(2);
            int maxRate = Math.min(productionRate, module.getContainerEnergy().divideToInt(usage));
            long hydrogenUsed = 0;
            GasStack hydrogenStack = MekanismGases.HYDROGEN.getStack(maxRate * 2L);
            ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
            if (checkChestPlate(chestStack)) {
                Optional<IGasHandler> chestCapability = chestStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
                if (chestCapability.isPresent()) {
                    hydrogenUsed = maxRate * 2L - chestCapability.get().insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
                    hydrogenStack.shrink(hydrogenUsed);
                }
            }
            if (fillHeld.get()) {
                ItemStack handStack = player.getItemBySlot(EquipmentSlotType.MAINHAND);
                Optional<IGasHandler> handCapability = handStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
                if (handCapability.isPresent()) {
                    hydrogenUsed = maxRate * 2L - handCapability.get().insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
                }
            }
            int oxygenUsed = Math.min(maxRate, player.getMaxAirSupply() - player.getAirSupply());
            long used = Math.max((int) Math.ceil(hydrogenUsed / 2D), oxygenUsed);
            module.useEnergy(player, usage.multiply(used));
            player.setAirSupply(player.getAirSupply() + oxygenUsed);
        }
    }

    /**
     * Checks whether the given chestplate should be filled with hydrogen, if it can store hydrogen. Does not check whether the chestplate can store hydrogen.
     *
     * @param chestPlate the chestplate to check
     *
     * @return whether the given chestplate should be filled with hydrogen.
     */
    private boolean checkChestPlate(ItemStack chestPlate) {
        if (chestPlate.getItem() == MekanismItems.MEKASUIT_BODYARMOR.get()) {
            return MekanismAPI.getModuleHelper().load(chestPlate, MekanismModules.JETPACK_UNIT) != null;
        }
        return true;
    }

    private int getMaxRate(IModule<ModuleElectrolyticBreathingUnit> module) {
        return (int) Math.pow(2, module.getInstalledCount());
    }
}