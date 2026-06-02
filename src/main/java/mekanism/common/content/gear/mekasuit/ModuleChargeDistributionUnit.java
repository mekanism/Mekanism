package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.content.network.distribution.EnergySaveTarget.SaveHandler;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnergyUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@ParametersAreNotNullByDefault
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
            EnergyHandler energyHandler = module.getEnergyHandler(itemAccess);
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
        try (Transaction subTransaction = Transaction.open(transaction)) {
            EnergySaveTarget saveTarget = new EnergySaveTarget(4);
            ResourceHandler<ItemResource> armorSlots = LivingEntityEquipmentWrapper.of(player, EquipmentSlot.Type.HUMANOID_ARMOR);
            long availableEnergy = 0;
            for (int slot = 0, size = armorSlots.size(); slot < size; slot++) {
                //TODO - 26.1: Instead of just directly going off of energy containers, should we support charging other armor that exposes energy capabilities?
                IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(Capabilities.ENERGY.getCapability(ItemAccess.forHandlerIndexStrict(armorSlots, slot)));
                if (energyContainer != null) {
                    saveTarget.addHandler(SaveHandler.startSaveHandling(energyContainer, subTransaction));
                    //TODO - 26.1: Do we need to worry about overflow?
                    availableEnergy += energyContainer.getAmountAsLong();
                }
            }
            //If we only have one handler we can skip charging as it will all just go back into the chest piece
            if (saveTarget.getHandlerCount() > 1 && availableEnergy > 0) {
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
        int toCharge = MekanismConfig.gear.mekaSuitInventoryChargeRate.get();
        //If we have more energy available than our charge rate, stop calculating the amount available and just pretend we have the rate limit worth of energy
        int availableEnergy = Math.min(energyHandler.getAmountAsInt(), toCharge);
        if (availableEnergy == 0) {
            return;
        }
        //TODO - 26.1: Evaluate the below which basically manually reimplements ItemAccess#forPlayerSlot but using the corresponding handlers
        // as it uses a HandlerItemAccess instead of PlayerItemAccess, but I think that might be fine?
        PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
        int selectedSlot = player.getInventory().getSelectedSlot();
        // first try to charge mainhand/offhand item
        availableEnergy -= EnergyUtils.chargeContents(energyHandler, playerInv.getHandSlots(), availableEnergy, transaction);
        if (toCharge > 0) {
            //TODO - 26.1: Should this just use the following, and not care that it "tries" to insert into the held hand a second time?
            // toCharge -= CableUtils.chargeContents(energyContainer, playerInv.getMainSlots(), toCharge, transaction);
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                if (slot != selectedSlot) {
                    availableEnergy -= EnergyUtils.charge(energyHandler, ItemAccess.forHandlerIndexStrict(playerInv, slot), availableEnergy, transaction);
                    if (availableEnergy == 0) {
                        return;
                    }
                }
            }
            if (availableEnergy > 0 && Mekanism.hooks.curios.isLoaded()) {
                ResourceHandler<ItemResource> handler = CuriosIntegration.getCuriosInventory(player);
                if (handler != null) {
                    EnergyUtils.chargeContents(energyHandler, handler, availableEnergy, transaction);
                }
            }
        }
    }
}