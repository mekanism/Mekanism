package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot input(@Nullable IContentsListener listener, TagKey<Upgrade> supportedTypes) {
        Objects.requireNonNull(supportedTypes, "Supported types cannot be null");
        return new UpgradeInventorySlot(ConstantPredicates.notExternal(), (itemType, _) -> {
            Holder<Upgrade> upgradeType = itemType.get(IUpgradeHelper.INSTANCE.dataComponent());
            return upgradeType != null && upgradeType.is(supportedTypes);
        }, listener);
    }

    public static UpgradeInventorySlot output(@Nullable IContentsListener listener) {
        return new UpgradeInventorySlot(ConstantPredicates.manualOnly(), ConstantPredicates.internalOnly(), listener);
    }

    private UpgradeInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert, @Nullable IContentsListener listener) {
        super(canExtract, canInsert, itemType -> itemType.has(IUpgradeHelper.INSTANCE.dataComponent()), null, null, listener, 0, 0);
        setSlotOverlay(SlotOverlay.UPGRADE);
    }

    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, new SelectedWindowData(WindowType.UPGRADE), getSlotOverlay());
    }
}