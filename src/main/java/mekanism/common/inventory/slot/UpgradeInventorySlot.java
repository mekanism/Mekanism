package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Upgrade;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.item.Item;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot of(@Nullable IMekanismInventory inventory, Set<Upgrade> supportedTypes) {
        Objects.requireNonNull(supportedTypes, "Supported types cannot be null");
        return new UpgradeInventorySlot(inventory, supportedTypes);
    }

    private final Set<Upgrade> supportedTypes;

    private UpgradeInventorySlot(@Nullable IMekanismInventory inventory, Set<Upgrade> supportedTypes) {
        super(manualOnly, (stack, automationType) -> {
            Item item = stack.getItem();
            if (item instanceof IUpgradeItem) {
                Upgrade upgradeType = ((IUpgradeItem) item).getUpgradeType(stack);
                return supportedTypes.contains(upgradeType);
            }
            return false;
        }, stack -> stack.getItem() instanceof IUpgradeItem, inventory, 154, 7);
        this.supportedTypes = supportedTypes;
        setSlotOverlay(SlotOverlay.UPGRADE);
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedTypes;
    }
}