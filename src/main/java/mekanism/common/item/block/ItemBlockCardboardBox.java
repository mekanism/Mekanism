package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().stacksTo(16));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, YesNo.of(getBlockData(stack) != null)));
        BlockData data = getBlockData(stack);
        if (data != null) {
            try {
                tooltip.add(MekanismLang.BLOCK.translate(data.blockState.getBlock()));
                if (data.tileTag != null) {
                    tooltip.add(MekanismLang.BLOCK_ENTITY.translate(data.tileTag.getString(NBTConstants.ID)));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean canReplace(Level world, Player player, BlockPos pos, Direction sideClicked, BlockState state, ItemStack stack) {
        //Check if the player is allowed to use the cardboard box in the given position
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos.relative(sideClicked), sideClicked, stack)) {
            //If they are then check if they can "break" the block that is in that spot
            if (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, player))) {
                //If they can then we need to see if they are allowed to "place" the cardboard box in the given position
                //TODO: Once forge fixes https://github.com/MinecraftForge/MinecraftForge/issues/7609 use block snapshots
                // and fire a place event to see if the player is able to "place" the cardboard box
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (getBlockData(stack) == null && !player.isShiftKeyDown()) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(world, pos) != -1) {
                if (state.is(MekanismTags.Blocks.CARDBOARD_BLACKLIST) ||
                    MekanismConfig.general.cardboardModBlacklist.get().contains(state.getBlock().getRegistryName().getNamespace()) ||
                    !canReplace(world, player, pos, context.getClickedFace(), state, stack)) {
                    return InteractionResult.FAIL;
                }
                BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile != null) {
                    if (MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST_LOOKUP.contains(tile.getType()) ||
                        !MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                        //If the tile is in the tile entity type blacklist or the player cannot access the tile
                        // don't allow them to pick it up with a cardboard box
                        return InteractionResult.FAIL;
                    }
                }
                if (!world.isClientSide) {
                    BlockData data = new BlockData(state);
                    if (tile != null) {
                        //Note: We check security access above
                        data.tileTag = tile.saveWithFullMetadata();
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    //Mark that we are monitoring item drops that might have been created due to using the cardboard box
                    // and then replace the block with the cardboard box, which will cause items to drop and then get
                    // cancelled by our listener in CommonWorldTickHandler
                    CommonWorldTickHandler.monitoringCardboardBox = true;
                    world.setBlockAndUpdate(pos, getBlock().defaultBlockState().setValue(BlockStateHelper.storageProperty, true));
                    CommonWorldTickHandler.monitoringCardboardBox = false;
                    TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                    if (box != null) {
                        box.storedData = data;
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        Level world = context.getLevel();
        if (world.isClientSide) {
            return true;
        }
        if (super.placeBlock(context, state)) {
            TileEntityCardboardBox tile = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, context.getClickedPos());
            if (tile != null) {
                tile.storedData = getBlockData(context.getItemInHand());
            }
            return true;
        }
        return false;
    }

    public void setBlockData(ItemStack stack, BlockData data) {
        ItemDataUtils.setCompound(stack, NBTConstants.DATA, data.write(new CompoundTag()));
    }

    public BlockData getBlockData(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.DATA, Tag.TAG_COMPOUND)) {
            return BlockData.read(ItemDataUtils.getCompound(stack, NBTConstants.DATA));
        }
        return null;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        BlockData blockData = getBlockData(stack);
        if (blockData != null) {
            return 1;
        }
        return super.getItemStackLimit(stack);
    }
}