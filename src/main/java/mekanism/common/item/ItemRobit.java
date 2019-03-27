package mekanism.common.item;

import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRobit extends ItemEnergized implements ISustainedInventory {

    public ItemRobit() {
        super(100000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);

        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.name") + ": " + EnumColor.GREY + getName(itemstack));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + (
              getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer entityplayer, World world, BlockPos pos, EnumHand hand,
          EnumFacing side, float posX, float posY, float posZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack itemstack = entityplayer.getHeldItem(hand);

        if (tileEntity instanceof TileEntityChargepad) {
            TileEntityChargepad chargepad = (TileEntityChargepad) tileEntity;

            if (!chargepad.isActive) {
                if (!world.isRemote) {
                    EntityRobit robit = new EntityRobit(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);

                    robit.setHome(Coord4D.get(chargepad));
                    robit.setEnergy(getEnergy(itemstack));
                    robit.setOwnerUUID(entityplayer.getUniqueID());
                    robit.setInventory(getInventory(itemstack));
                    robit.setCustomNameTag(getName(itemstack));

                    world.spawnEntity(robit);
                }

                entityplayer.setHeldItem(hand, ItemStack.EMPTY);

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    public void setName(ItemStack itemstack, String name) {
        ItemDataUtils.setString(itemstack, "name", name);
    }

    public String getName(ItemStack itemstack) {
        String name = ItemDataUtils.getString(itemstack, "name");

        return name.isEmpty() ? "Robit" : name;
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
