package mekanism.common.inventory.container.item;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismItemContainer extends MekanismContainer {

    protected final InteractionHand hand;
    protected final ItemStack stack;

    protected MekanismItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(type, id, inv);
        this.hand = hand;
        this.stack = stack;
        if (!stack.isEmpty()) {
            //It shouldn't be empty but validate it just in case
            addContainerTrackers();
        }
        addSlotsAndOpen();
    }

    protected void addContainerTrackers() {
        if (stack.getItem() instanceof IItemContainerTracker containerTracker) {
            containerTracker.addContainerTrackers(this, stack);
        }
    }

    @Override
    protected void addInventorySlots(@NotNull Inventory inv) {
        super.addInventorySlots(inv);
        if (offhandSlots.isEmpty()) {
            //If we don't have a slot relating to offhand data, add a syncable itemstack to track any changes that might happen to the stack
            // as some of them may need to be reflected in the GUI https://github.com/mekanism/Mekanism/issues/7923
            track(SyncableItemStack.create(inv.player::getOffhandItem, item -> inv.player.setItemSlot(EquipmentSlot.OFFHAND, item)));
        }
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return IItemSecurityUtils.INSTANCE.canAccess(player, stack);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !this.stack.isEmpty() && player.getItemInHand(this.hand).is(this.stack.getItem());
    }

    public interface IItemContainerTracker {

        void addContainerTrackers(MekanismContainer container, ItemStack stack);
    }
}