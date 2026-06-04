package mekanism.common.lib.inventory.personalstorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@MethodsAreNotNullByDefault
@ParametersAreNotNullByDefault
@EventBusSubscriber(modid = Mekanism.MODID)
public class PersonalStorageManager {

    @Nullable
    private static SavedDataStorage DATA_STORAGE;
    private static final Map<UUID, PersonalStorageData> STORAGE_BY_PLAYER_UUID = new HashMap<>();

    @Nullable
    private static PersonalStorageData forOwner(UUID playerUUID) {
        if (EffectiveSide.get().isClient()) {
            return null;
        }
        return STORAGE_BY_PLAYER_UUID.computeIfAbsent(
              playerUUID,
              uuid -> Objects.requireNonNull(dataStorage().computeIfAbsent(PersonalStorageData.createType(uuid)), "Failed to create data")
        );
    }

    private static SavedDataStorage dataStorage() {
        return Objects.requireNonNull(DATA_STORAGE, "Illegal state");
    }

    /**
     * Only call on the server. Gets or creates an inventory for the supplied stack
     *
     * @param itemAccess Personal storage Item Access (type not checked) - will be modified if it didn't have an inventory id
     *
     * @return the existing or new inventory
     */
    @Nullable
    public static AbstractPersonalStorageItemInventory getInventoryFor(ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
        if (owner == null) {
            Mekanism.logger.error("Storage inventory asked for but item type has no owner! {}", itemAccess.getResource(), new Exception());
            return null;
        }
        return getInventoryFor(itemAccess, owner, transaction);
    }

    /**
     * Only call on the server. Gets or creates an inventory for the supplied stack
     *
     * @param itemAccess Personal storage Item Access (type not checked) - will be modified if it didn't have an inventory id
     * @param owner      The owner of the stack
     *
     * @return the existing or new inventory
     */
    @Nullable
    public static AbstractPersonalStorageItemInventory getInventoryFor(ItemAccess itemAccess, UUID owner, @Nullable TransactionContext transaction) {
        UUID invId = getInventoryId(itemAccess, transaction);
        return getInventoryForUnchecked(invId, owner);
    }

    /**
     * Only call on the server. Gets an inventory for the supplied stack
     *
     * @param inventoryId Personal storage inventory id
     * @param owner       The owner of the stack
     *
     * @return the existing or new inventory
     */
    @Nullable
    public static AbstractPersonalStorageItemInventory getInventoryForUnchecked(@Nullable UUID inventoryId, UUID owner) {
        if (inventoryId == null) {
            return null;
        }
        PersonalStorageData data = forOwner(owner);
        if (data != null) {
            return data.getOrAddInventory(inventoryId);
        }
        return null;
    }

    public static boolean createInventoryFor(ItemAccess itemAccess, List<IInventorySlot> contents, TransactionContext transaction) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
        if (owner == null || contents.size() != 54) {
            //No owner or wrong number of slots, something went wrong
            return false;
        }
        //Get a new inventory id
        PersonalStorageData data = forOwner(owner);
        if (data != null) {
            data.addInventory(getInventoryId(itemAccess, transaction), contents);
            return true;
        }
        return false;
    }

    /**
     * Only call on the server
     * <p>
     * Version of {@link #getInventoryFor(ItemAccess, TransactionContext)} which will NOT create an inventory if none exists already. The stack will only be modified if
     * it contained a legacy inventory
     *
     * @param itemAccess Personal storage Item Access
     *
     * @return the existing or converted inventory, or null if none exists in saved data nor legacy data
     */
    @Nullable
    public static AbstractPersonalStorageItemInventory getInventoryIfPresent(ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
        return owner != null && itemAccess.getResource().has(MekanismDataComponents.PERSONAL_STORAGE_ID) ? getInventoryFor(itemAccess, owner, transaction) : null;
    }

    public static void deleteInventory(ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
        if (owner != null) {
            ItemResource resource = itemAccess.getResource();
            UUID storageId = resource.get(MekanismDataComponents.PERSONAL_STORAGE_ID);
            if (storageId != null) {
                //TODO - 26.1: Do we want this to fail if we couldn't exchange for some reason?
                ItemAccessUtils.exchange(itemAccess, resource.without(MekanismDataComponents.PERSONAL_STORAGE_ID), transaction);
                //If there actually was an id stored then remove the corresponding inventory
                PersonalStorageData data = forOwner(owner);
                if (data != null) {
                    data.removeInventory(storageId);
                }
            }
        }
    }

    private static UUID getInventoryId(ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        ItemResource resource = itemAccess.getResource();
        UUID invId = resource.get(MekanismDataComponents.PERSONAL_STORAGE_ID);
        if (invId == null) {
            invId = UUID.randomUUID();
            //TODO - 26.1: Do we want this to fail if we couldn't exchange for some reason?
            ItemAccessUtils.exchange(itemAccess, resource.with(MekanismDataComponents.PERSONAL_STORAGE_ID, invId), transaction);
        }
        return invId;
    }

    public static void reset() {
        STORAGE_BY_PLAYER_UUID.clear();
    }

    public static void createSlots(Consumer<IInventorySlot> slotConsumer, BiPredicate<ItemResource, AutomationType> canInteract, @Nullable IContentsListener listener) {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                slotConsumer.accept(BasicInventorySlot.at(canInteract, canInteract, listener, 8 + slotX * 18, 18 + slotY * 18));
            }
        }
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        DATA_STORAGE = event.getServer().getDataStorage();
    }

    @SubscribeEvent
    static void serverStopped(ServerStoppedEvent ignored) {
        DATA_STORAGE = null;
    }
}