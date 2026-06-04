package mekanism.common.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.loot.PersonalStorageContentsLootFunction;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPersonalStorage<TILE extends TileEntityPersonalStorage, BLOCK extends BlockTypeTile<TILE>> extends BlockTile<TILE, BLOCK> {

    public static final Attribute PERSONAL_STORAGE_INVENTORY = new AttributeInventory<>(lootBuilder -> {
        lootBuilder.apply(PersonalStorageContentsLootFunction.builder());
        return true;
    });

    public BlockPersonalStorage(BLOCK type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide() && stack.count() == 1 && (!(placer instanceof Player player) || !player.getAbilities().instabuild)) {
            //itemstack will be deleted, remove the stored inventory
            try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                PersonalStorageManager.deleteInventory(ItemAccess.forStack(stack), transaction);
                transaction.commit();
            }
        }
    }
}