package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.ChargeUtils;
import net.minecraft.item.ItemStack;

public class EnergyInventorySlot extends BasicInventorySlot {

    //Cache the predicates as we only really need one instance of them
    private static final Predicate<@NonNull ItemStack> dischargeExtractPredicate = stack -> ChargeUtils.canBeOutputted(stack, false);
    private static final Predicate<@NonNull ItemStack> dischargeInsertPredicate = ChargeUtils::canBeDischarged;
    private static final Predicate<@NonNull ItemStack> chargeExtractPredicate = stack -> ChargeUtils.canBeOutputted(stack, true);
    private static final Predicate<@NonNull ItemStack> chargeInsertPredicate = ChargeUtils::canBeCharged;
    private static final Predicate<@NonNull ItemStack> validPredicate = ChargeUtils::isEnergyItem;

    /**
     * Takes energy from the item
     */
    public static EnergyInventorySlot discharge(IMekanismInventory inventory, int x, int y) {
        return new EnergyInventorySlot(inventory, x, y, true);
        //TODO: ChargeUtils.discharge(this, tile);
    }

    /**
     * Gives energy to the item
     */
    public static EnergyInventorySlot charge(IMekanismInventory inventory, int x, int y) {
        return new EnergyInventorySlot(inventory, x, y, false);
        //TODO: ChargeUtils.charge(this, tile);
    }

    //TODO: Use this for charging/discharging
    private final boolean discharge;

    private EnergyInventorySlot(IMekanismInventory inventory, int x, int y, boolean discharge) {
        super(discharge ? dischargeExtractPredicate : chargeExtractPredicate, discharge ? dischargeInsertPredicate : chargeInsertPredicate, validPredicate, inventory, x, y);
        this.discharge = discharge;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.POWER;
    }

    //TODO: Make these support charging/discharging the item instead of having to manually specify it in the tiles
}