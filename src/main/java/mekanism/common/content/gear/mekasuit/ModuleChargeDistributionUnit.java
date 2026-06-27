package mekanism.common.content.gear.mekasuit;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.content.network.distribution.EnergySaveTarget.SaveHandler;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnergyUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record ModuleChargeDistributionUnit(boolean chargeSuit, boolean chargeInventory) implements ICustomModule<ModuleChargeDistributionUnit> {

    public static final Identifier CHARGE_SUIT = Mekanism.rl("charge_suit");
    public static final Identifier CHARGE_INVENTORY = Mekanism.rl("charge_inventory");

    public ModuleChargeDistributionUnit(IModule<ModuleChargeDistributionUnit> module) {
        this(module.getBooleanConfigOrFalse(CHARGE_SUIT), module.getBooleanConfigOrFalse(CHARGE_INVENTORY));
    }

    @Override
    public void tickServer(IModule<ModuleChargeDistributionUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        // charge inventory first
        if (chargeInventory) {
            EnergyHandler energyHandler = module.getEnergyHandler(itemAccess, false);
            if (energyHandler != null) {
                chargeInventory(energyHandler, player, transaction);
            }
        }
        // distribute suit charge next, so that if we used power from the suit to charge an item, then we can balance across the suit properly
        if (chargeSuit) {
            chargeSuit(player, transaction);
        }
    }

    private void chargeSuit(Player player, TransactionContext transaction) {
        ResourceHandler<ItemResource> armorSlots = LivingEntityEquipmentWrapper.of(player, EquipmentSlot.Type.HUMANOID_ARMOR);
        int size = armorSlots.size();
        long availableEnergy = 0;
        List<IEnergyContainer> energyContainers = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) {
            IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(Capabilities.ENERGY.getCapability(ItemAccess.forHandlerIndexStrict(armorSlots, slot)));
            if (energyContainer != null) {
                energyContainers.add(energyContainer);
                availableEnergy += energyContainer.getAmountAsLong();
                if (availableEnergy < 0) {//TODO: Is there any way we can cleanly support this case? Maybe doing multiple distributions?
                    Mekanism.logger.warn("Failed to distribute energy across worn armor due to having more than max long energy.");
                    return;
                }
            }
        }
        //If we only have one handler we can skip charging as it will all just go back into the chest piece
        if (energyContainers.size() > 1 && availableEnergy > 0) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                EnergySaveTarget saveTarget = new EnergySaveTarget(energyContainers.size());
                for (IEnergyContainer energyContainer : energyContainers) {
                    saveTarget.addHandler(SaveHandler.startSaveHandling(energyContainer, subTransaction));
                }
                long distributed = EmitUtils.sendToAcceptors(saveTarget, availableEnergy, EnergyNetwork.ENERGY, subTransaction);
                if (distributed == availableEnergy) {
                    subTransaction.commit();
                } else {
                    Mekanism.logger.warn("Failed to distribute {} energy across {} pieces of armor. {} energy remaining afterward.", availableEnergy,
                          saveTarget.getHandlerCount(), availableEnergy - distributed);
                }
            }
        }
    }

    private void chargeInventory(EnergyHandler energyHandler, Player player, TransactionContext transaction) {
        //Only try to charge up to how much energy we actually have stored
        int availableEnergy = Math.min(energyHandler.getAmountAsInt(), MekanismConfig.gear.mekaSuitInventoryChargeRate.get());
        if (availableEnergy > 0) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Validate against any potential rate limit of the item
                availableEnergy = energyHandler.extract(availableEnergy, simulation);
            }
        }
        if (availableEnergy > 0) {
            PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
            int selectedSlot = player.getInventory().getSelectedSlot();
            // first try to charge mainhand/offhand item
            availableEnergy -= EnergyUtils.chargeContents(energyHandler, playerInv.getHandSlots(), availableEnergy, transaction);
            if (availableEnergy > 0) {
                availableEnergy -= EnergyUtils.chargeContents(energyHandler, playerInv.getMainSlots(), availableEnergy, transaction, selectedSlot);
                if (availableEnergy > 0) {
                    ResourceHandler<ItemResource> curiosInventory = Mekanism.hooks.getCuriosInventory(player);
                    if (curiosInventory != null) {
                        EnergyUtils.chargeContents(energyHandler, curiosInventory, availableEnergy, transaction);
                    }
                }
            }
        }
    }
}