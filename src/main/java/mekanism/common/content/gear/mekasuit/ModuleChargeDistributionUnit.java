package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.content.network.distribution.EnergySaveTarget.DelegateSaveHandler;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.CableUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
    public void tickServer(IModule<ModuleChargeDistributionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        // charge inventory first
        if (chargeInventory) {
            IEnergyContainer energyContainer = module.getEnergyContainer(stack);
            if (energyContainer != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    chargeInventory(energyContainer, player, transaction);
                    transaction.commit();
                }
            }
        }
        // distribute suit charge next, so that if we used power from the suit to charge an item, then we can balance across the suit properly
        if (chargeSuit) {
            chargeSuit(player);
        }
    }

    private void chargeSuit(Player player) {
        EnergySaveTarget<DelegateSaveHandler> saveTarget = new EnergySaveTarget<>(4);
        for (ItemStack stack : MekanismUtils.getArmorSlots(player)) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null) {
                saveTarget.addHandler(new DelegateSaveHandler(energyContainer));
            }
        }
        if (saveTarget.getHandlerCount() > 1) {
            //If we only have one handler we can skip charging as it will all just go back into the chest piece
            long stored = saveTarget.getStored();
            //TODO - 26.1: Re-evaluate how we handle transactions for energy
            try (Transaction transaction = Transaction.openRoot()) {
                EmitUtils.sendToAcceptors(saveTarget, stored, EnergyNetwork.ENERGY, transaction);
                saveTarget.save();
                transaction.commit();
            }
        }
    }

    private void chargeInventory(IEnergyContainer energyContainer, Player player, TransactionContext transaction) {
        //Only try to charge up to how much energy we actually have stored
        long toCharge = Math.min(MekanismConfig.gear.mekaSuitInventoryChargeRate.get(), energyContainer.getEnergy());
        if (toCharge == 0L) {
            return;
        }
        //TODO - 26.1: Review usages of ItemAccess#forPlayerInteraction to see if any should bypass the infinite materials check like ItemAccess#forPlayerSlot allows
        //TODO - 26.1: Evaluate the below which basically manually reimplements ItemAccess#forPlayerSlot but using the corresponding handlers
        // as it uses a HandlerItemAccess instead of PlayerItemAccess, but I think that might be fine?
        PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
        int selectedSlot = player.getInventory().getSelectedSlot();
        // first try to charge mainhand/offhand item
        toCharge -= CableUtils.chargeContents(energyContainer, playerInv.getHandSlots(), toCharge, transaction);
        if (toCharge > 0L) {
            //TODO - 26.1: Should this just use the following, and not care that it "tries" to insert into the held hand a second time?
            // toCharge -= CableUtils.chargeContents(energyContainer, playerInv.getMainSlots(), toCharge, transaction);
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                if (slot != selectedSlot) {
                    toCharge -= CableUtils.charge(energyContainer, ItemAccess.forHandlerIndexStrict(playerInv, slot), toCharge, transaction);
                    if (toCharge == 0L) {
                        return;
                    }
                }
            }
            if (toCharge > 0 && Mekanism.hooks.curios.isLoaded()) {
                ResourceHandler<ItemResource> handler = CuriosIntegration.getCuriosInventory(player);
                if (handler != null) {
                    CableUtils.chargeContents(energyContainer, handler, toCharge, transaction);
                }
            }
        }
    }
}