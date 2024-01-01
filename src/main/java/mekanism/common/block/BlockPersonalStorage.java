package mekanism.common.block;

import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.loot.PersonalStorageContentsLootFunction;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPersonalStorage<TILE extends TileEntityPersonalStorage, BLOCK extends BlockTypeTile<TILE>> extends BlockTile<TILE, BLOCK> {

    public static final Attribute PERSONAL_STORAGE_INVENTORY = new AttributeInventory<>(((lootBuilder, nbtBuilder) -> {
        lootBuilder.apply(PersonalStorageContentsLootFunction.builder());
        return true;
    }));

    public BlockPersonalStorage(BLOCK type, UnaryOperator<Properties> propertiesModifier) {
        super(type, propertiesModifier);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide) {
            PersonalStorageManager.getInventoryIfPresent(stack).ifPresent(storageItemInventory -> {
                TileEntityPersonalStorage tile = WorldUtils.getTileEntity(TileEntityPersonalStorage.class, world, pos);
                if (tile == null) {
                    return;
                }
                List<IInventorySlot> inventorySlots = storageItemInventory.getInventorySlots(null);
                for (int i = 0; i < inventorySlots.size(); i++) {
                    IInventorySlot itemSlot = inventorySlots.get(i);
                    tile.setStackInSlot(i, itemSlot.getStack().copy());
                }
                if (stack.getCount() == 1 && (!(placer instanceof Player player) || !player.getAbilities().instabuild)) {
                    //itemstack will be deleted, remove the stored inventory
                    PersonalStorageManager.deleteInventory(stack);
                }
            });
        }
    }
}