package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    private static boolean isMonitoring;

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(16));
        //TODO: Listen to event as needed
        //MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.blockData"), ": ", YesNo.of(getBlockData(stack) != null)));
        BlockData data = getBlockData(stack);
        if (data != null) {
            try {
                tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.block"), ": " + new ItemStack(data.block).getDisplayName()));
                if (data.tileTag != null) {
                    tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.tile"), ": " + data.tileTag.getString("id")));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = player.getHeldItem(context.getHand());
        if (!player.isSneaking() && !world.isAirBlock(pos) && stack.getDamage() == 0) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!world.isRemote && MekanismAPI.isBlockCompatible(block) && state.getBlockHardness(world, pos) != -1) {
                BlockData data = new BlockData();
                data.block = block;
                isMonitoring = true;
                TileEntityCardboardBox tile = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                if (tile != null) {
                    CompoundNBT tag = new CompoundNBT();
                    tile.write(tag);
                    data.tileTag = tag;
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                // First, set the block to air to give the underlying block a chance to process
                // any updates (esp. if it's a tile entity backed block). Ideally, we could avoid
                // double updates, but if the block we are wrapping has multiple stacked blocks,
                // we need to make sure it has a chance to update.
                world.removeBlock(pos, false);
                world.setBlockState(pos, getBlock().getDefaultState().with(BlockStateHelper.storageProperty, true));
                isMonitoring = false;
                if (tile != null) {
                    tile.storedData = data;
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
            TileEntityCardboardBox tile = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, context.getPos());
            if (tile != null) {
                tile.storedData = getBlockData(context.getItem());
            }
            return true;
        }
        return false;
    }

    public void setBlockData(ItemStack stack, BlockData data) {
        ItemDataUtils.setCompound(stack, "blockData", data.write(new CompoundNBT()));
    }

    public BlockData getBlockData(ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, "blockData")) {
            return null;
        }
        return BlockData.read(ItemDataUtils.getCompound(stack, "blockData"));
    }

    //TODO
    /*@SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && isMonitoring) {
            event.setCanceled(true);
        }
    }*/

    @Override
    public int getItemStackLimit(ItemStack stack) {
        BlockData blockData = getBlockData(stack);
        if (blockData != null) {
            return 1;
        }
        return super.getItemStackLimit(stack);
    }
}