package mekanism.common.item.loot;

import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.ClientSidePersonalStorageInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * Loot function which copies the Personal Storage inventory to the saved data and adds an inv id to the stack
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageContentsLootFunction implements LootItemFunction {

    public static final PersonalStorageContentsLootFunction INSTANCE = new PersonalStorageContentsLootFunction();

    private PersonalStorageContentsLootFunction() {
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public LootItemFunctionType<PersonalStorageContentsLootFunction> getType() {
        return MekanismLootFunctions.PERSONAL_STORAGE.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileEntityPersonalStorage personalStorage && !personalStorage.isInventoryEmpty()) {
            List<IInventorySlot> tileSlots = personalStorage.getInventorySlots(null);
            AbstractPersonalStorageItemInventory destInv;
            if (EffectiveSide.get().isClient()) {
                destInv = new ClientSidePersonalStorageInventory();
            } else {
                destInv = PersonalStorageManager.getInventoryFor(stack).orElseThrow(() -> new IllegalStateException("Inventory not available?!"));
            }
            for (int i = 0; i < tileSlots.size(); i++) {
                IInventorySlot tileSlot = tileSlots.get(i);
                if (!tileSlot.isEmpty()) {
                    destInv.setStackInSlot(i, tileSlot.getStack().copy());
                }
            }
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return MekanismLootFunctions.BLOCK_ENTITY_LOOT_CONTEXT;
    }
}