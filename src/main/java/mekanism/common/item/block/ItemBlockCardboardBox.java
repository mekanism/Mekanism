package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    private static boolean isMonitoring;

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, new Item.Properties().maxStackSize(16));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.blockData"), ": ", BooleanStateDisplay.YesNo.of(getBlockData(itemstack) != null)));
        BlockData data = getBlockData(itemstack);
        if (data != null) {
            try {
                tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.block"), ": " + new ItemStack(data.block).getDisplayName()));
                if (data.tileTag != null) {
                    tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.tile"), ": " + data.tileTag.getString("id")));
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
                if (world.getTileEntity(pos) != null) {
                    TileEntity tile = world.getTileEntity(pos);
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
                TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);
                if (tileEntity != null) {
                    tileEntity.storedData = data;
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
            TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(context.getPos());
            if (tileEntity != null) {
                tileEntity.storedData = getBlockData(context.getItem());
            }
            return true;
        }
        return false;
    }

    public void setBlockData(ItemStack itemstack, BlockData data) {
        ItemDataUtils.setCompound(itemstack, "blockData", data.write(new CompoundNBT()));
    }

    public BlockData getBlockData(ItemStack itemstack) {
        if (!ItemDataUtils.hasData(itemstack, "blockData")) {
            return null;
        }
        return BlockData.read(ItemDataUtils.getCompound(itemstack, "blockData"));
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && isMonitoring) {
            event.setCanceled(true);
        }
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