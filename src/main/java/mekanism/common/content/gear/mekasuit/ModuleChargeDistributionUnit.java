package mekanism.common.content.gear.mekasuit;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class ModuleChargeDistributionUnit extends ModuleMekaSuit {

    private ModuleConfigItem<Boolean> chargeSuit;
    private ModuleConfigItem<Boolean> chargeInventory;

    @Override
    public void init() {
        super.init();
        chargeSuit = addConfigItem(new ModuleConfigItem<>(this, "charge_suit", MekanismLang.MODULE_CHARGE_SUIT, new BooleanData(), true));
        chargeInventory = addConfigItem(new ModuleConfigItem<>(this, "charge_inventory", MekanismLang.MODULE_CHARGE_INVENTORY, new BooleanData(), false));
    }

    @Override
    public void tickServer(PlayerEntity player) {
        super.tickServer(player);
        // charge inventory first
        if (chargeInventory.get()) {
            chargeInventory(player);
        }
        // distribute suit charge next
        if (chargeSuit.get()) {
            chargeSuit(player);
        }
    }

    private void chargeSuit(PlayerEntity player) {
        Set<EnergySaveTarget> saveTargets = new ObjectOpenHashSet<>();
        FloatingLong total = FloatingLong.ZERO;
        for (ItemStack stack : player.inventory.armorInventory) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null) {
                EnergySaveTarget saveTarget = new EnergySaveTarget();
                saveTarget.addHandler(Direction.NORTH, energyContainer);
                saveTargets.add(saveTarget);
                total = total.plusEqual(energyContainer.getEnergy());
            }
        }
        EmitUtils.sendToAcceptors(saveTargets, saveTargets.size(), total.copy());
        for (EnergySaveTarget saveTarget : saveTargets) {
            saveTarget.save(Direction.NORTH);
        }
    }

    private void chargeInventory(PlayerEntity player) {
        FloatingLong toCharge = MekanismConfig.gear.mekaSuitInventoryChargeRate.get();
        // first try to charge mainhand/offhand item
        toCharge = charge(player, player.getHeldItemMainhand(), toCharge);
        toCharge = charge(player, player.getHeldItemOffhand(), toCharge);

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack == player.getHeldItemMainhand() || stack == player.getHeldItemOffhand()) {
                continue;
            }
            if (toCharge.isZero()) {
                break;
            }
            toCharge = charge(player, stack, toCharge);
        }
    }

    /** return rejects */
    private FloatingLong charge(PlayerEntity player, ItemStack stack, FloatingLong amount) {
        IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        if (handler != null) {
            FloatingLong remaining = handler.insertEnergy(amount, Action.SIMULATE);
            if (remaining.smallerThan(amount)) {
                //If we can actually insert any energy into
                return handler.insertEnergy(useEnergy(player, amount.subtract(remaining), false), Action.EXECUTE).add(remaining);
            }
        }
        return amount;
    }
}