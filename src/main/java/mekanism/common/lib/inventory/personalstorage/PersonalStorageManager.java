package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageManager {
    private static final Map<UUID, PersonalStorageData> STORAGE_BY_PLAYER_UUID = new HashMap<>();

    private static PersonalStorageData forOwner(UUID playerUUID) {
        return STORAGE_BY_PLAYER_UUID.computeIfAbsent(playerUUID, uuid->MekanismSavedData.createSavedData(PersonalStorageData::new, "personal_storage" + File.separator + uuid));
    }

    /**
     * Only call on the server
     *
     * @param stack Personal storage ItemStack (type not checked) - will be modified if it didn't have an inventory id
     * @return the existing or new inventory
     */
    public static PersonalStorageItemInventory getInventoryFor(ItemStack stack) {
        UUID owner = SecurityUtils.get().getOwnerUUID(stack);
        if (owner == null) {
            throw new IllegalStateException("Stack has no owner!");
        }
        UUID invId = getInventoryId(stack);
        return forOwner(owner).getOrAddInventory(invId);
    }

    @NotNull
    private static UUID getInventoryId(ItemStack stack) {
        UUID invId = ItemDataUtils.getUniqueID(stack, NBTConstants.PERSONAL_STORAGE_ID);
        if (invId == null) {
            invId = UUID.randomUUID();
            ItemDataUtils.setUUID(stack, NBTConstants.PERSONAL_STORAGE_ID, invId);
        }
        return invId;
    }

    public static void reset() {
        STORAGE_BY_PLAYER_UUID.clear();
    }

}
