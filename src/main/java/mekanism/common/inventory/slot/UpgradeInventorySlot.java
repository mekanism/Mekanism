package mekanism.common.inventory.slot;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.base.IUpgradeItem;
import net.minecraft.item.Item;

public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot of(IMekanismInventory inventory, Set<Upgrade> supportedTypes) {
        return new UpgradeInventorySlot(inventory, supportedTypes);
    }

    private final Set<Upgrade> supportedTypes;

    private UpgradeInventorySlot(IMekanismInventory inventory, Set<Upgrade> supportedTypes) {
        super(manualOnly, (stack, automationType) -> {
            Item item = stack.getItem();
            if (item instanceof IUpgradeItem) {
                Upgrade upgradeType = ((IUpgradeItem) item).getUpgradeType(stack);
                return supportedTypes.contains(upgradeType);
            }
            return false;
        }, stack -> stack.getItem() instanceof IUpgradeItem, inventory, 154, 7);
        this.supportedTypes = supportedTypes;
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedTypes;
    }
}