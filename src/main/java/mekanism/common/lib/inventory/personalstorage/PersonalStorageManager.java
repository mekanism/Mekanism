package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageManager {
    private static final Map<UUID, PersonalStorageData> STORAGE_BY_PLAYER_UUID = new HashMap<>();

    private static Optional<PersonalStorageData> forOwner(UUID playerUUID) {
        if (EffectiveSide.get().isClient()) {
            return Optional.empty();
        }
        return Optional.of(STORAGE_BY_PLAYER_UUID.computeIfAbsent(playerUUID, uuid->MekanismSavedData.createSavedData(PersonalStorageData::new, "personal_storage" + File.separator + uuid)));
    }

    /**
     * Only call on the server. Gets or creates an inventory for the supplied stack
     *
     * @param stack Personal storage ItemStack (type not checked) - will be modified if it didn't have an inventory id
     * @return the existing or new inventory
     */
    public static Optional<AbstractPersonalStorageItemInventory> getInventoryFor(ItemStack stack) {
        UUID owner = SecurityUtils.get().getOwnerUUID(stack);
        if (owner == null) {
            Mekanism.logger.error("Storage inventory asked for but stack has no owner! {}", stack, new Exception());
            return Optional.empty();
        }
        UUID invId = getInventoryId(stack);
        return forOwner(owner).map(data->{
            AbstractPersonalStorageItemInventory storageItemInventory = data.getOrAddInventory(invId);
            //TODO - After 1.20: Remove legacy loading
            ListTag legacyData = ItemDataUtils.getList(stack, NBTConstants.ITEMS);
            if (!legacyData.isEmpty()) {
                DataHandlerUtils.readContainers(storageItemInventory.getInventorySlots(null), legacyData);
                ItemDataUtils.removeData(stack, NBTConstants.ITEMS);
            }

            return storageItemInventory;
        });
    }

    public static boolean createInventoryFor(ItemStack stack, List<IInventorySlot> contents) {
        UUID owner = SecurityUtils.get().getOwnerUUID(stack);
        if (owner == null || contents.size() != 54) {
            //No owner or wrong number of slots, something went wrong
            return false;
        }
        //Get a new inventory id
        UUID invId = getInventoryId(stack);
        forOwner(owner).ifPresent(inv->inv.addInventory(invId, contents));
        return true;
    }

    /**
     * Only call on the server
     * <p>
     * Version of {@link #getInventoryFor(ItemStack)} which will NOT create an inventory if none exists already.
     * The stack will only be modified if it contained a legacy inventory
     *
     * @param stack Personal storage ItemStack
     * @return the existing or converted inventory, or an empty optional if none exists in saved data nor legacy data
     */
    public static Optional<AbstractPersonalStorageItemInventory> getInventoryIfPresent(ItemStack stack) {
        UUID owner = SecurityUtils.get().getOwnerUUID(stack);
        UUID invId = getInventoryIdNullable(stack);
        //TODO - After 1.20: Remove legacy loading
        boolean hasLegacyData = ItemDataUtils.hasData(stack, NBTConstants.ITEMS, Tag.TAG_LIST);
        return owner != null && (invId != null || hasLegacyData) ? getInventoryFor(stack) : Optional.empty();
    }

    public static void deleteInventory(ItemStack stack) {
        UUID owner = SecurityUtils.get().getOwnerUUID(stack);
        UUID invId = getInventoryIdNullable(stack);
        if (owner != null && invId != null) {
            forOwner(owner).ifPresent(handler->handler.removeInventory(invId));
        }
    }

    @NotNull
    private static UUID getInventoryId(ItemStack stack) {
        UUID invId = getInventoryIdNullable(stack);
        if (invId == null) {
            invId = UUID.randomUUID();
            ItemDataUtils.setUUID(stack, NBTConstants.PERSONAL_STORAGE_ID, invId);
        }
        return invId;
    }

    @Nullable
    private static UUID getInventoryIdNullable(ItemStack stack) {
        return ItemDataUtils.getUniqueID(stack, NBTConstants.PERSONAL_STORAGE_ID);
    }

    public static void reset() {
        STORAGE_BY_PLAYER_UUID.clear();
    }

    public static void createSlots(Consumer<IInventorySlot> slotConsumer, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInteract,
          @Nullable IContentsListener listener) {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                slotConsumer.accept(BasicInventorySlot.at(canInteract, canInteract, listener, 8 + slotX * 18, 18 + slotY * 18));
            }
        }
    }

}