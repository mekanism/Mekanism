package mekanism.common.inventory.slot;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.base.IUpgradeItem;
import net.minecraft.item.Item;

public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot of(Set<Upgrade> supportedTypes) {
        return new UpgradeInventorySlot(supportedTypes);
    }

    private final Set<Upgrade> supportedTypes;

    private UpgradeInventorySlot(Set<Upgrade> supportedTypes) {
        super(alwaysFalse, stack -> {
            Item item = stack.getItem();
            if (item instanceof IUpgradeItem) {
                Upgrade upgradeType = ((IUpgradeItem) item).getUpgradeType(stack);
                return supportedTypes.contains(upgradeType);
            }
            return false;
        }, stack -> stack.getItem() instanceof IUpgradeItem, 154, 7);
        this.supportedTypes = supportedTypes;
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedTypes;
    }
}