/**
 * 
 */
package mekanism.induction.common.item;

import java.util.List;

import mekanism.api.Object3D;
import mekanism.common.item.ItemMekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public abstract class ItemCoordLink extends ItemMekanism
{
	public ItemCoordLink(String name, int id)
	{
		super(id);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasLink(itemstack))
		{
			Object3D obj = getLink(itemstack);
			int dimID = getLinkDim(itemstack);

			list.add("Bound to [" + (int) obj.xCoord + ", " + (int) obj.yCoord + ", " + (int) obj.zCoord + "], dimension '" + dimID + "'");
		}
		else
		{
			list.add("No block bound");
		}
	}

	public boolean hasLink(ItemStack itemStack)
	{
		return getLink(itemStack) != null;
	}

	public Object3D getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("position"))
		{
			return null;
		}
		return Object3D.read(itemStack.getTagCompound().getCompoundTag("position"));
	}

	public void setLink(ItemStack itemStack, Object3D obj, int dimID)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setCompoundTag("position", obj.write(new NBTTagCompound()));

		itemStack.stackTagCompound.setInteger("dimID", dimID);
	}

	public int getLinkDim(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getInteger("dimID");
	}

	public void clearLink(ItemStack itemStack)
	{
		itemStack.getTagCompound().removeTag("position");
		itemStack.getTagCompound().removeTag("dimID");
	}
}
