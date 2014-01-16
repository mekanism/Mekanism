package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterNetwork;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.input.Keyboard;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartTransmitter extends JItemMultiPart
{
	public ItemPartTransmitter(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {}

	@Override
	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord coord, int face, Vector3 vecHit)
	{
		TransmitterType type = TransmitterType.values()[stack.getItemDamage()];
		
		if(type.getTransmission() != TransmissionType.ITEM)
		{
			Coord4D obj = new Coord4D(coord.x, coord.y, coord.z, world.provider.dimensionId);
			
			List<ITransmitterNetwork<?, ?>> networks = new ArrayList<ITransmitterNetwork<?, ?>>();
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = obj.getFromSide(side).getTileEntity(world);
				
				if(tile instanceof IGridTransmitter && TransmissionType.checkTransmissionType(tile, type.getTransmission()))
				{
					networks.add(((IGridTransmitter)tile).getTransmitterNetwork());
				}
			}
			
			if(networks.size() > 0)
			{
				if(!networks.iterator().next().canMerge(networks))
				{
					return null;
				}
			}
		}
		
		return PartTransmitter.getPartType(TransmitterType.values()[getDamage(stack)]);
	}
	
	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			if(itemstack.getItemDamage() < Tier.CableTier.values().length)
			{
				list.add(EnumColor.INDIGO + "Capacity: " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(Tier.CableTier.values()[itemstack.getItemDamage()].cableCapacity) + "/t");
			}
			
			list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " for details.");
		}
		else {
			switch(itemstack.getItemDamage())
			{
				case 0: case 1: case 2: case 3:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "RF " + EnumColor.GREY + "(ThermalExpansion)");
					list.add("- " + EnumColor.PURPLE + "EU " + EnumColor.GREY +  "(IndustrialCraft)");
					list.add("- " + EnumColor.PURPLE + "MJ " + EnumColor.GREY +  "(BuildCraft)");
					list.add("- " + EnumColor.PURPLE + "Joules " + EnumColor.GREY +  "(Mekanism)");
					break;
				}
				case 4:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "mB " + EnumColor.GREY + "(FluidRegistry)");
					break;
				}
				case 5:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "O " + EnumColor.GREY + "(Oxygen)");
					list.add("- " + EnumColor.PURPLE + "H " + EnumColor.GREY + "(Hydrogen)");
					break;
				}
				case 6:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "Items (universal)");
					list.add("- " + EnumColor.PURPLE + "Blocks (universal)");
					break;
				}
				case 7:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "Items (universal)");
					list.add("- " + EnumColor.PURPLE + "Blocks (universal)");
					list.add("- " + EnumColor.DARK_RED + "Only used if no other paths available");
					break;
				}
				case 8:
				{
					list.add(EnumColor.DARK_GREY + "Capable of transferring:");
					list.add("- " + EnumColor.PURPLE + "Items (universal)");
					list.add("- " + EnumColor.PURPLE + "Blocks (universal)");
					list.add("- " + EnumColor.DARK_RED + "Controllable by redstone");
					break;
				}
			}
		}
	}
	
    @Override
    public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
    {
        for(TransmitterType type : TransmitterType.values())
        {
            listToAddTo.add(new ItemStack(itemID, 1, type.ordinal()));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber()
    {
    	return 0;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
    	return getUnlocalizedName() + "." + TransmitterType.values()[stack.getItemDamage()].getName();
    }
}
