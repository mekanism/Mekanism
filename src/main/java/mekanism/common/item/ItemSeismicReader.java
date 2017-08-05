package mekanism.common.item;

import mekanism.api.Chunk3D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemSeismicReader extends ItemEnergized
{
	public static final double ENERGY_USAGE = 250;
	
	public ItemSeismicReader()
	{
		super(12000);
	}
	
	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, EnumHand hand)
	{
		Chunk3D chunk = new Chunk3D(entityplayer);
		ItemStack itemstack = entityplayer.getHeldItem(hand);
		
		if(getEnergy(itemstack) < ENERGY_USAGE && !entityplayer.capabilities.isCreativeMode)
		{
			if(!world.isRemote)
			{
				entityplayer.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.RED + LangUtils.localize("tooltip.seismicReader.needsEnergy")));
			}
			
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
		else if(!MekanismUtils.isChunkVibrated(chunk))
		{
			if(!world.isRemote)
			{
				entityplayer.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.RED + LangUtils.localize("tooltip.seismicReader.noVibrations")));
			}
			
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
		
		if(!entityplayer.capabilities.isCreativeMode)
		{
			setEnergy(itemstack, getEnergy(itemstack)-ENERGY_USAGE);
		}
		
		entityplayer.openGui(Mekanism.instance, 38, world, hand.ordinal(), 0, 0);

		return new ActionResult<>(EnumActionResult.PASS, itemstack);
	}
}
