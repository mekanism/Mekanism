package mekanism.common.content.gear.mekasuit;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.content.network.distribution.EnergySaveTarget.DelegateSaveHandler;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

@ParametersAreNotNullByDefault
public record ModuleChargeDistributionUnit(boolean chargeSuit, boolean chargeInventory) implements ICustomModule<ModuleChargeDistributionUnit> {

    public static final ResourceLocation CHARGE_SUIT = Mekanism.rl("charge_suit");
    public static final ResourceLocation CHARGE_INVENTORY = Mekanism.rl("charge_inventory");

    public ModuleChargeDistributionUnit(IModule<ModuleChargeDistributionUnit> module) {
        this(module.getBooleanConfigOrFalse(CHARGE_SUIT), module.getBooleanConfigOrFalse(CHARGE_INVENTORY));
    }

    @Override
    public void tickServer(IModule<ModuleChargeDistributionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        // charge inventory first
        if (chargeInventory) {
            IEnergyContainer energyContainer = module.getEnergyContainer(stack);
            if (energyContainer != null) {
                chargeInventory(energyContainer, player);
            }
        }
        // distribute suit charge next, so that if we used power from the suit to charge an item, then we can balance across the suit properly
        if (chargeSuit) {
            chargeSuit(player);
        }
    }

    private void chargeSuit(Player player) {
        EnergySaveTarget<DelegateSaveHandler> saveTarget = new EnergySaveTarget<>(4);
        for (ItemStack stack : player.getArmorSlots()) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null) {
                saveTarget.addHandler(new DelegateSaveHandler(energyContainer));
            }
        }
        if (saveTarget.getHandlerCount() > 1) {
            //If we only have one handler we can skip charging as it will all just go back into the chest piece
            long stored = saveTarget.getStored();
            EmitUtils.sendToAcceptors(saveTarget, stored, EnergyNetwork.ENERGY);
            saveTarget.save();
        }
    }

    private void chargeInventory(IEnergyContainer energyContainer, Player player) {
        //Only try to charge up to how much energy we actually have stored
        long toCharge = Math.min(MekanismConfig.gear.mekaSuitInventoryChargeRate.get(), energyContainer.getEnergy());
        if (toCharge == 0L) {
            return;
        }
        // first try to charge mainhand/offhand item
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        toCharge = charge(energyContainer, mainHand, toCharge);
        toCharge = charge(energyContainer, offHand, toCharge);
        if (toCharge > 0L) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack != mainHand && stack != offHand) {
                    toCharge = charge(energyContainer, stack, toCharge);
                    if (toCharge == 0L) {
                        return;
                    }
                }
            }
            if (Mekanism.hooks.curios.isLoaded()) {
                IItemHandler handler = CuriosIntegration.getCuriosInventory(player);
                if (handler != null) {
                    for (int slot = 0, slots = handler.getSlots(); slot < slots; slot++) {
                        toCharge = charge(energyContainer, handler.getStackInSlot(slot), toCharge);
                        if (toCharge == 0L) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /** return rejects */
    private long charge(IEnergyContainer energyContainer, ItemStack stack, long amount) {
        if (!stack.isEmpty() && amount > 0L) {
            IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (handler != null) {
                long remaining = handler.insertEnergy(amount, Action.SIMULATE);
                if (remaining < amount) {
                    //If we can actually insert any energy into
                    long toExtract = amount - remaining;
                    long extracted = energyContainer.extract(toExtract, Action.EXECUTE, AutomationType.MANUAL);
                    long inserted = handler.insertEnergy(extracted, Action.EXECUTE);
                    return inserted + remaining;
                }
            }
        }
        return amount;
    }
}