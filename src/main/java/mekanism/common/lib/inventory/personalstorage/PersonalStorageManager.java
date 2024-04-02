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
import mekanism.api.IContentsListener;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
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
        return Optional.of(STORAGE_BY_PLAYER_UUID.computeIfAbsent(playerUUID, uuid -> MekanismSavedData.createSavedData(PersonalStorageData::new, "personal_storage" + File.separator + uuid)));
    }

    /**
     * Only call on the server. Gets or creates an inventory for the supplied stack
     *
     * @param stack Personal storage ItemStack (type not checked) - will be modified if it didn't have an inventory id
     *
     * @return the existing or new inventory
     */
    public static Optional<AbstractPersonalStorageItemInventory> getInventoryFor(ItemStack stack) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
        if (owner == null) {
            Mekanism.logger.error("Storage inventory asked for but stack has no owner! {}", stack, new Exception());
            return Optional.empty();
        }
        UUID invId = getInventoryId(stack);
        Optional<PersonalStorageData> data = forOwner(owner);
        //noinspection OptionalIsPresent - Capturing lambda
        if (data.isPresent()) {
            return Optional.of(data.get().getOrAddInventory(invId));
        }
        return Optional.empty();
    }

    public static boolean createInventoryFor(ItemStack stack, List<IInventorySlot> contents) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
        if (owner == null || contents.size() != 54) {
            //No owner or wrong number of slots, something went wrong
            return false;
        }
        //Get a new inventory id
        forOwner(owner).ifPresent(inv -> inv.addInventory(getInventoryId(stack), contents));
        return true;
    }

    /**
     * Only call on the server
     * <p>
     * Version of {@link #getInventoryFor(ItemStack)} which will NOT create an inventory if none exists already. The stack will only be modified if it contained a legacy
     * inventory
     *
     * @param stack Personal storage ItemStack
     *
     * @return the existing or converted inventory, or an empty optional if none exists in saved data nor legacy data
     */
    public static Optional<AbstractPersonalStorageItemInventory> getInventoryIfPresent(ItemStack stack) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
        return owner != null && stack.hasData(MekanismAttachmentTypes.PERSONAL_STORAGE_ID) ? getInventoryFor(stack) : Optional.empty();
    }

    public static void deleteInventory(ItemStack stack) {
        UUID owner = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
        if (owner != null) {
            UUID storageId = stack.removeData(MekanismAttachmentTypes.PERSONAL_STORAGE_ID);
            if (storageId != null) {
                //If there actually was an id stored then remove the corresponding inventory
                forOwner(owner).ifPresent(handler -> handler.removeInventory(storageId));
            }
        }
    }

    @NotNull
    private static UUID getInventoryId(ItemStack stack) {
        Optional<UUID> existingData = stack.getExistingData(MekanismAttachmentTypes.PERSONAL_STORAGE_ID);
        if (existingData.isPresent()) {
            return existingData.get();
        }
        UUID invId = UUID.randomUUID();
        stack.setData(MekanismAttachmentTypes.PERSONAL_STORAGE_ID, invId);
        return invId;
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