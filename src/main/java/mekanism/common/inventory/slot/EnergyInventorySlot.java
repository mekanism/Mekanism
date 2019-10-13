package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.ChargeUtils;
import net.minecraft.item.ItemStack;

public class EnergyInventorySlot extends BasicInventorySlot {

    //Cache the predicates as we only really need one instance of them
    private static final Predicate<@NonNull ItemStack> dischargeExtractPredicate = item -> ChargeUtils.canBeOutputted(item, false);
    private static final Predicate<@NonNull ItemStack> dischargeInsertPredicate = ChargeUtils::canBeDischarged;
    private static final Predicate<@NonNull ItemStack> chargeExtractPredicate = item -> ChargeUtils.canBeOutputted(item, true);
    private static final Predicate<@NonNull ItemStack> chargeInsertPredicate = ChargeUtils::canBeCharged;
    private static final Predicate<@NonNull ItemStack> validPredicate = ChargeUtils::isEnergyItem;

    /**
     * Takes energy from the item
     */
    public static EnergyInventorySlot discharge(int x, int y) {
        return new EnergyInventorySlot(x, y, true);
        //TODO: ChargeUtils.discharge(this, tile);
    }

    /**
     * Gives energy to the item
     */
    public static EnergyInventorySlot charge(int x, int y) {
        return new EnergyInventorySlot(x, y, false);
        //TODO: ChargeUtils.charge(this, tile);
    }

    //TODO: Use this for charging/discharging
    private final boolean isCharge;

    private EnergyInventorySlot(int x, int y, boolean isCharge) {
        super(isCharge ? chargeExtractPredicate : dischargeExtractPredicate, isCharge ? chargeInsertPredicate : dischargeInsertPredicate, validPredicate, x, y);
        this.isCharge = isCharge;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.POWER;
    }

    //TODO: Make these support charging/discharging the item instead of having to manually specify it in the tiles
}