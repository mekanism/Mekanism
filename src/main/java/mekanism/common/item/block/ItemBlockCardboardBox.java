package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.BlockEvent;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().stacksTo(16));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, YesNo.of(getBlockData(stack) != null)));
        BlockData data = getBlockData(stack);
        if (data != null) {
            try {
                tooltip.add(MekanismLang.BLOCK.translate(data.blockState.getBlock()));
                if (data.tileTag != null) {
                    tooltip.add(MekanismLang.TILE.translate(data.tileTag.getString(NBTConstants.ID)));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean canReplace(World world, PlayerEntity player, BlockPos pos, Direction sideClicked, BlockState state, ItemStack stack) {
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
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (stack.isEmpty() || player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (getBlockData(stack) == null && !player.isShiftKeyDown()) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir(world, pos) && state.getDestroySpeed(world, pos) != -1) {
                if (state.is(MekanismTags.Blocks.CARDBOARD_BLACKLIST) ||
                    MekanismConfig.general.cardboardModBlacklist.get().contains(state.getBlock().getRegistryName().getNamespace()) ||
                    !canReplace(world, player, pos, context.getClickedFace(), state, stack)) {
                    return ActionResultType.FAIL;
                }
                TileEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile != null) {
                    if (tile.getType().isIn(MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST) || !SecurityUtils.canAccess(player, tile)) {
                        //If the tile is in the tile entity type blacklist or the player cannot access the tile
                        // don't allow them to pick it up with a cardboard box
                        return ActionResultType.FAIL;
                    }
                }
                if (!world.isClientSide) {
                    BlockData data = new BlockData(state);
                    if (tile != null) {
                        //Note: We check security access above
                        CompoundNBT tag = new CompoundNBT();
                        tile.save(tag);
                        data.tileTag = tag;
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    //Start by removing the tile entity so that there is no backing inventory to have dropped
                    // when we change the block to a cardboard box.
                    // Note: If this starts causing issues switch back to a monitorCardboardBox styled system
                    // for stopping item drops and try to find a way of getting it to work properly with custom
                    // item entities that are added a tick later (such as trying to get forge to change their
                    // listener from highest to high)
                    world.removeBlockEntity(pos);
                    world.setBlockAndUpdate(pos, getBlock().defaultBlockState().setValue(BlockStateHelper.storageProperty, true));
                    TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                    if (box != null) {
                        box.storedData = data;
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getLevel();
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
        ItemDataUtils.setCompound(stack, NBTConstants.DATA, data.write(new CompoundNBT()));
    }

    public BlockData getBlockData(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.DATA, NBT.TAG_COMPOUND)) {
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