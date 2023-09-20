package mekanism.common.item.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Loot function which copies the Personal Storage inventory to the saved data and adds an inv id to the stack
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageContentsLootFunction implements LootItemFunction {

    private static final PersonalStorageContentsLootFunction INSTANCE = new PersonalStorageContentsLootFunction();
    private static final Set<LootContextParam<?>> REFERENCED_PARAMS = Set.of(LootContextParams.BLOCK_ENTITY);

    private PersonalStorageContentsLootFunction() {
    }

    public static LootItemFunction.Builder builder() {
        return ()->INSTANCE;
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.PERSONAL_STORAGE_LOOT_FUNC.get();
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileEntityPersonalStorage personalStorage && !personalStorage.isInventoryEmpty()) {
            List<IInventorySlot> tileSlots = personalStorage.getInventorySlots(null);
            PersonalStorageItemInventory destInv = PersonalStorageManager.getInventoryFor(itemStack);
            for (int i = 0; i < tileSlots.size(); i++) {
                IInventorySlot tileSlot = tileSlots.get(i);
                if (!tileSlot.isEmpty()) {
                    destInv.setStackInSlot(i, tileSlot.getStack().copy());
                }
            }
        }
        return itemStack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return REFERENCED_PARAMS;
    }

    public static class PersonalStorageLootFunctionSerializer implements Serializer<PersonalStorageContentsLootFunction> {

        @Override
        public void serialize(JsonObject pJson, PersonalStorageContentsLootFunction pValue, JsonSerializationContext pSerializationContext) {
            //no-op
        }

        @Override
        public PersonalStorageContentsLootFunction deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
            return INSTANCE;
        }
    }
}