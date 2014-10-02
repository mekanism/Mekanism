package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemElectricBow extends ItemEnergized
{
	public ItemElectricBow()
	{
		super(120000);
		setFull3D();
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		
		list.add(EnumColor.PINK + MekanismUtils.localize("tooltip.fireMode") + ": " + EnumColor.GREY + LangUtils.transOnOff(getFireState(itemstack)));
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int itemUseCount)
	{
		if(getEnergy(itemstack) > 0)
		{
			boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemstack) > 0;

			if(flag || player.inventory.hasItem(Items.arrow))
			{
				int maxItemUse = getMaxItemUseDuration(itemstack) - itemUseCount;
				float f = maxItemUse / 20F;
				f = (f * f + f * 2.0F) / 3F;

				if(f < 0.1D)
				{
					return;
				}

				if(f > 1.0F)
				{
					f = 1.0F;
				}

				EntityArrow entityarrow = new EntityArrow(world, player, f * 2.0F);

				if(f == 1.0F)
				{
					entityarrow.setIsCritical(true);
				}

				if(!player.capabilities.isCreativeMode)
				{
					setEnergy(itemstack, getEnergy(itemstack) - (getFireState(itemstack) ? 1200 : 120));
				}

				world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

				if(flag)
				{
					entityarrow.canBePickedUp = 2;
				}
				else {
					player.inventory.consumeInventoryItem(Items.arrow);
				}

				if(!world.isRemote)
				{
					world.spawnEntityInWorld(entityarrow);
					entityarrow.setFire(getFireState(itemstack) ? 60 : 0);
				}
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemstack)
	{
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack)
	{
		return EnumAction.bow;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(entityplayer.capabilities.isCreativeMode || entityplayer.inventory.hasItem(Items.arrow))
		{
			entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
		}

		return itemstack;
	}

	/**
	 * Sets the bow's fire state in NBT.
	 * @param itemstack - the bow's itemstack
	 * @param state - state to change to
	 */
	public void setFireState(ItemStack itemstack, boolean state)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setBoolean("fireState", state);
	}

	/**
	 * Gets the bow's fire state from NBT.
	 * @param itemstack - the bow's itemstack
	 * @return fire state
	 */
	public boolean getFireState(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return false;
		}

		boolean state = false;

		if(itemstack.stackTagCompound.getTag("fireState") != null)
		{
			state = itemstack.stackTagCompound.getBoolean("fireState");
		}

		return state;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
