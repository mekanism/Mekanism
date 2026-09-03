package mekanism.common.block;

import java.util.Optional;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.component.BlockData;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

public class BlockCardboardBox extends BlockMekanism implements IStateStorage, IHasTileEntity<TileEntityCardboardBox> {

    public BlockCardboardBox(BlockBehaviour.Properties properties) {
        super(properties.strength(0.5F, 0.6F).mapColor(MapColor.WOOD));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        } else if (!canReplace(world, player, pos, state)) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            Optional<BlockData> blockData = Optional.ofNullable(WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos))
                  .map(box -> box.components().getOrDefault(MekanismDataComponents.BLOCK_DATA, BlockData.NONE))
                  .filter(BlockData::hasData);
            if (blockData.isPresent()) {
                if (!blockData.get().tryPlaceIntoWorld(world, pos, player)) {
                    //Can't place it into the world, skip
                    return InteractionResult.PASS;
                }
                popResource(world, pos, new ItemStack(MekanismBlocks.CARDBOARD_BOX));
                MekanismCriteriaTriggers.UNBOX_CARDBOARD_BOX.value().trigger((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean canReplace(Level world, Player player, BlockPos pos, BlockState state) {
        //Check if the player is allowed to use the cardboard box in the given position
        if (world.mayInteract(player, pos)) {
            //If they are then check if they can "break" the cardboard block that is in that spot
            //Note: We call this on both the client and the server
            if (!NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, pos, state, player)).isCanceled()) {
                //If they can then we need to see if they are allowed to "place" the unboxed block in the given position
                //TODO: Once forge fixes https://github.com/MinecraftForge/MinecraftForge/issues/7609 use block snapshots
                // and fire a place event to see if the player is able to "place" the cardboard box
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null && BlockData.hasData(context.getItemInHand())) {
            return state.setValue(BlockStateHelper.storageProperty, true);
        }
        return state;
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityCardboardBox> getTileType() {
        return MekanismTileEntityTypes.CARDBOARD_BOX;
    }
}