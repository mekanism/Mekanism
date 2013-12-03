/**
 * 
 */
package mekanism.induction.common.item;

import java.util.List;

import mekanism.induction.common.tileentity.TileEntityMultimeter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * ItemBlock for the Multimeter
 * 
 * @author Calclavia
 * 
 */
public class ItemBlockMultimeter extends ItemBlock
{
	public ItemBlockMultimeter(int par1)
	{
		super(par1);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		par3List.add("Shift-right click to place,");
		par3List.add("Right click to scan data.");

		float detection = this.getDetection(itemStack);

		if (detection != -1)
		{
			par3List.add("Last Detection: " + detection + " KJ");
		}
		else
		{
			par3List.add("No detection saved.");
		}
	}

	@Override
	public boolean onItemUseFirst(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!par2EntityPlayer.isSneaking())
		{
			// if (!world.isRemote)
			{
				par2EntityPlayer.addChatMessage("Energy: " + TileEntityMultimeter.getDetectedEnergy(ForgeDirection.getOrientation(par7), world.getBlockTileEntity(x, y, z)) + " J");
			}

			return true;
		}

		return super.onItemUseFirst(par1ItemStack, par2EntityPlayer, world, x, y, z, par7, par8, par9, par10);
	}

	public float getDetection(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("detection"))
		{
			return -1;
		}

		return itemStack.stackTagCompound.getFloat("detection");
	}

	public void setDetection(ItemStack itemStack, float detection)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("detection", detection);
	}
}
