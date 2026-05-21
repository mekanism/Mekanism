package mekanism.common.inventory.container.item;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.PortableQIODashboardInventory;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.item.MekanismItemContainer.IItemContainerTracker;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.qio.BulkQIOData;
import mekanism.common.network.to_server.PacketItemGuiInteract;
import mekanism.common.network.to_server.PacketItemGuiInteract.ItemGuiInteraction;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardContainer extends QIOItemViewerContainer {

    protected final InteractionHand hand;
    protected final ItemAccess itemAccess;
    private QIOFrequency freq;

    public PortableQIODashboardContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess, boolean remote, BulkQIOData itemData) {
        this(id, inv, hand, itemAccess, remote, new PortableQIODashboardInventory(inv.player.level(), itemAccess), itemData,
              remote ? CachedSearchData.initialClient() : CachedSearchData.INITIAL_SERVER,
              remote ? CachedSortingData.currentClient() : CachedSortingData.SERVER,
              null, null);
    }

    private PortableQIODashboardContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder,
          BulkQIOData itemData, CachedSearchData searchData, CachedSortingData sortingData, @Nullable SelectedWindowData selectedWindow, QIOFrequency freq) {
        super(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, id, inv, remote, craftingWindowHolder, itemData, searchData, sortingData, selectedWindow);
        this.hand = hand;
        this.freq = freq;
        this.itemAccess = itemAccess;
        if (isValidType(this.itemAccess.getResource())) {
            addContainerTrackers();
        }
        addSlotsAndOpen();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemResource getItemType() {
        return itemAccess.getResource();
    }

    @Override
    protected PortableQIODashboardContainer recreateUnchecked() {
        return new PortableQIODashboardContainer(containerId, inv, hand, itemAccess, true, craftingWindowHolder, asBulkData(), asCachedSearchData(), currentSortingData(),
              getSelectedWindow(), freq);
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        if (craftingWindowHolder instanceof PortableQIODashboardInventory inventory && inventory.getLevel() != null && inventory.getLevel().isClientSide()) {
            //If we are on the client side, use our local stored frequency
            return freq;
        }
        return super.getFrequency();
    }

    protected void addContainerTrackers() {
        if (itemAccess.getResource().getItem() instanceof IItemContainerTracker containerTracker) {
            containerTracker.addContainerTrackers(this, itemAccess);
        }
        track(SyncableFrequency.create(FrequencyTypes.QIO, this::getFrequency, f -> freq = f));
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
    protected HotBarSlot createHotBarSlot(@NotNull Inventory inv, int index, int x, int y) {
        // special handling to prevent removing the dashboard from the player's inventory slot
        if (index == inv.getSelectedSlot() && hand == InteractionHand.MAIN_HAND) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ContainerInput clickType, @NotNull Player player) {
        if (clickType == ContainerInput.SWAP) {
            if (hand == InteractionHand.OFF_HAND && dragType == Inventory.SLOT_OFFHAND) {
                //Block pressing f to swap it when it is in the offhand
                return;
            } else if (hand == InteractionHand.MAIN_HAND && Inventory.isHotbarSlot(dragType)) {
                //Block taking out of the selected slot (we don't validate we have a hotbar slot as we always should for this container)
                if (!hotBarSlots.get(dragType).mayPickup(player)) {
                    return;
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return IItemSecurityUtils.INSTANCE.canAccess(player, itemAccess);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return isValidType(itemAccess.getResource());
    }

    protected boolean isValidType(ItemResource itemType) {
        return !itemType.isEmpty() && MekanismItems.PORTABLE_QIO_DASHBOARD.is(itemType);
    }

    @Override
    public boolean shiftClickIntoFrequency() {
        ItemResource resource = itemAccess.getResource();
        //Shouldn't be empty, as otherwise stillValid would fail, but validate it just in case
        return !resource.isEmpty() && resource.getOrDefault(MekanismDataComponents.INSERT_INTO_FREQUENCY, true);
    }

    @Override
    public void toggleTargetDirection() {
        //Change the data client side so that it is reflected in the gui as we don't handle updating client side data
        PacketUtils.sendToServer(new PacketItemGuiInteract(ItemGuiInteraction.TARGET_DIRECTION_BUTTON, this.hand));
    }
}
