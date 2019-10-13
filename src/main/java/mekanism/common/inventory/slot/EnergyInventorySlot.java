package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.common.util.ChargeUtils;
import net.minecraft.item.ItemStack;

public class EnergyInventorySlot extends BasicInventorySlot {

    //Cache the predicates as we only really need one instance of them
    private static final Predicate<@NonNull ItemStack> extractPredicate = item -> ChargeUtils.canBeOutputted(item, false);
    private static final Predicate<@NonNull ItemStack> insertPredicate = ChargeUtils::canBeDischarged;
    private static final Predicate<@NonNull ItemStack> validPredicate = ChargeUtils::isEnergyItem;

    public EnergyInventorySlot() {
        super(extractPredicate, insertPredicate, validPredicate);
    }

    //TODO: ChargeUtils.discharge move to here, or make it accept a EnergyInventorySlot?
}