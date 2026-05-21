package mekanism.common.inventory.container.item;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismItemContainer extends MekanismContainer {

    protected final InteractionHand hand;
    protected final ItemAccess itemAccess;

    protected MekanismItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(type, id, inv);
        this.hand = hand;
        this.itemAccess = itemAccess;
        if (isValidType(this.itemAccess.getResource())) {
            addContainerTrackers();
        }
        addSlotsAndOpen();
    }

    protected void addContainerTrackers() {
        if (itemAccess.getResource().getItem() instanceof IItemContainerTracker containerTracker) {
            containerTracker.addContainerTrackers(this, itemAccess);
        }
    }

    protected boolean isValidType(ItemResource itemType) {
        return !itemType.isEmpty();
    }

    @Override
    protected void addInventorySlots(@NotNull Inventory inv) {
        super.addInventorySlots(inv);
        if (offhandSlots.isEmpty()) {
            //If we don't have a slot relating to offhand data, add a syncable itemstack to track any changes that might happen to the stack
            // as some of them may need to be reflected in the GUI https://github.com/mekanism/Mekanism/issues/7923
            track(SyncableItemStack.create(inv.player::getOffhandItem, item -> inv.player.setItemSlot(EquipmentSlot.OFFHAND, item)));
        }
        if (hotBarSlots.isEmpty()) {
            //If we don't have a slot relating to hotbar data, add syncable itemstacks to track any changes to the main hand
            // as some of them may need to be reflected in the GUI https://github.com/mekanism/Mekanism/issues/8020
            if (hand == InteractionHand.MAIN_HAND) {
                track(SyncableItemStack.create(inv.player::getMainHandItem, item -> inv.player.setItemSlot(EquipmentSlot.MAINHAND, item)));
            }
            //And we need to sync the other hotbar slots as well, so that their durability bars update if they are based on things like energy
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                if (i != inv.getSelectedSlot() || hand != InteractionHand.MAIN_HAND) {
                    int index = i;
                    track(SyncableItemStack.create(() -> inv.getItem(index), item -> inv.setItem(index, item)));
                }
            }
        }
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return IItemSecurityUtils.INSTANCE.canAccess(player, itemAccess);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return isValidType(itemAccess.getResource());
    }

    @FunctionalInterface
    public interface IItemContainerTracker {

        void addContainerTrackers(MekanismContainer container, ItemAccess itemAccess);
    }
}