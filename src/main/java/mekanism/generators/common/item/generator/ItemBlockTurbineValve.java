package mekanism.generators.common.item.generator;

import javax.annotation.Nonnull;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.ItemDataUtils;
import mekanism.generators.common.block.generator.BlockTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockTurbineValve extends ItemBlockTooltip implements ISustainedInventory {

    public ItemBlockTurbineValve(BlockTurbineValve block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityTurbineValve tile = (TileEntityTurbineValve) world.getTileEntity(pos);
            if (tile != null) {
                //Sustained Inventory
                tile.setInventory(getInventory(stack));
            }
            return true;
        }
        return false;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }
}