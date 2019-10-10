package mekanism.common.inventory.slot;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.base.IUpgradeItem;
import net.minecraft.item.Item;

public class UpgradeInventorySlot extends BasicInventorySlot {

    private final Set<Upgrade> supportedTypes;

    //TODO: Get this set from the block instead of the component
    public UpgradeInventorySlot(Set<Upgrade> supportedTypes) {
        super(item -> false, true, stack -> {
            Item item = stack.getItem();
            if (item instanceof IUpgradeItem) {
                Upgrade upgradeType = ((IUpgradeItem) item).getUpgradeType(stack);
                return supportedTypes.contains(upgradeType);
            }
            return false;
        });
        this.supportedTypes = supportedTypes;
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedTypes;
    }
}