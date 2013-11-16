/**
 * 
 */
package mekanism.induction.common.entangler;

import java.util.List;

import mekanism.induction.common.base.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public abstract class ItemCoordLink extends ItemBase
{
	public ItemCoordLink(String name, int id)
	{
		super(name, id);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasLink(itemstack))
		{
			Vector3 vec = getLink(itemstack);
			int dimID = getLinkDim(itemstack);

			list.add("Bound to [" + (int) vec.x + ", " + (int) vec.y + ", " + (int) vec.z + "], dimension '" + dimID + "'");
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

	public Vector3 getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("position"))
		{
			return null;
		}
		return new Vector3(itemStack.getTagCompound().getCompoundTag("position"));
	}

	public void setLink(ItemStack itemStack, Vector3 vec, int dimID)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setCompoundTag("position", vec.writeToNBT(new NBTTagCompound()));

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
