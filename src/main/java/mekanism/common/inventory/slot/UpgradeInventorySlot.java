package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.item.interfaces.IUpgradeItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UpgradeInventorySlot extends BasicInventorySlot {

    public static UpgradeInventorySlot input(@Nullable IContentsListener listener, Set<Upgrade> supportedTypes) {
        Objects.requireNonNull(supportedTypes, "Supported types cannot be null");
        return new UpgradeInventorySlot(listener, (stack, automationType) -> {
            Item item = stack.getItem();
            if (item instanceof IUpgradeItem upgradeItem) {
                Upgrade upgradeType = upgradeItem.getUpgradeType(stack);
                return supportedTypes.contains(upgradeType);
            }
            return false;
        });
    }

    public static UpgradeInventorySlot output(@Nullable IContentsListener listener) {
        return new UpgradeInventorySlot(listener, internalOnly);
    }

    private UpgradeInventorySlot(@Nullable IContentsListener listener, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert) {
        super(manualOnly, canInsert, stack -> stack.getItem() instanceof IUpgradeItem, listener, 0, 0);
        setSlotOverlay(SlotOverlay.UPGRADE);
    }

    @Nonnull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, new SelectedWindowData(WindowType.UPGRADE), getSlotOverlay(), this::setStackUnchecked);
    }
}