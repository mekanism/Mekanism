package mekanism.common.item.loot;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.ClientSidePersonalStorageInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import mekanism.common.util.InventoryUtils;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * Loot function which copies the Personal Storage inventory to the saved data and adds an inv id to the stack
 */
@MethodsAreNotNullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageContentsLootFunction implements LootItemFunction {

    public static final PersonalStorageContentsLootFunction INSTANCE = new PersonalStorageContentsLootFunction();
    public static final MapCodec<PersonalStorageContentsLootFunction> MAP_CODEC = MapCodec.unit(INSTANCE);

    private PersonalStorageContentsLootFunction() {
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public MapCodec<? extends LootItemFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileEntityPersonalStorage personalStorage) {
            List<IInventorySlot> tileSlots = personalStorage.getInventorySlots();
            //Validate that at least one slot has something stored
            if (!InventoryUtils.areContainersEmpty(tileSlots)) {
                AbstractPersonalStorageItemInventory destInv;
                if (EffectiveSide.get().isClient()) {
                    destInv = new ClientSidePersonalStorageInventory();
                } else {
                    destInv = Objects.requireNonNull(PersonalStorageManager.getInventoryFor(stack), "Inventory not available?!");
                }
                for (int i = 0, size = tileSlots.size(); i < size; i++) {
                    IInventorySlot tileSlot = tileSlots.get(i);
                    if (!tileSlot.isEmpty()) {
                        destInv.setContents(i, tileSlot.getResource(), tileSlot.amount());
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return MekanismLootFunctions.BLOCK_ENTITY_LOOT_CONTEXT;
    }
}