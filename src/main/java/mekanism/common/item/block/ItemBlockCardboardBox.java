package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonPlayerTracker;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(16));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
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

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (stack.isEmpty() || player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (getBlockData(stack) == null && !player.isSneaking()) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir(world, pos) && state.getBlockHardness(world, pos) != -1) {
                if (state.isIn(MekanismTags.Blocks.CARDBOARD_BLACKLIST) || MekanismConfig.general.cardboardModBlacklist.get().contains(state.getBlock().getRegistryName().getNamespace())) {
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
                if (!world.isRemote) {
                    BlockData data = new BlockData(state);
                    if (tile != null) {
                        //Note: We check security access above
                        CompoundNBT tag = new CompoundNBT();
                        tile.write(tag);
                        data.tileTag = tag;
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    CommonPlayerTracker.monitoringCardboardBox = true;
                    // First, set the block to air to give the underlying block a chance to process
                    // any updates (esp. if it's a tile entity backed block). Ideally, we could avoid
                    // double updates, but if the block we are wrapping has multiple stacked blocks,
                    // we need to make sure it has a chance to update.
                    //world.removeBlock(pos, false);
                    world.setBlockState(pos, getBlock().getDefaultState().with(BlockStateHelper.storageProperty, true));
                    CommonPlayerTracker.monitoringCardboardBox = false;
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
        World world = context.getWorld();
        if (world.isRemote) {
            return true;
        }
        if (super.placeBlock(context, state)) {
            TileEntityCardboardBox tile = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, context.getPos());
            if (tile != null) {
                tile.storedData = getBlockData(context.getItem());
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