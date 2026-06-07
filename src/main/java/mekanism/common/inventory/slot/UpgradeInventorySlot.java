package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.item.interfaces.IUpgradeItem;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot input(@Nullable IContentsListener listener, Set<Upgrade> supportedTypes) {
        Objects.requireNonNull(supportedTypes, "Supported types cannot be null");
        return new UpgradeInventorySlot(ConstantPredicates.notExternal(), (itemType, _) -> {
            if (itemType.getItem() instanceof IUpgradeItem upgradeItem) {
                Upgrade upgradeType = upgradeItem.getUpgradeType();
                return supportedTypes.contains(upgradeType);
            }
            return false;
        }, listener);
    }

    public static UpgradeInventorySlot output(@Nullable IContentsListener listener) {
        return new UpgradeInventorySlot(ConstantPredicates.manualOnly(), ConstantPredicates.internalOnly(), listener);
    }

    private UpgradeInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert, @Nullable IContentsListener listener) {
        super(canExtract, canInsert, itemType -> itemType.getItem() instanceof IUpgradeItem, null, null, listener, 0, 0);
        setSlotOverlay(SlotOverlay.UPGRADE);
    }

    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, new SelectedWindowData(WindowType.UPGRADE), getSlotOverlay());
    }
}