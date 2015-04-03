package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartTransmitter extends JItemMultiPart
{
	public ItemPartTransmitter()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}

	@Override
	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord coord, int face, Vector3 vecHit)
	{
		TransmitterType type = TransmitterType.values()[stack.getItemDamage()];

		if(type.getTransmission() != TransmissionType.ITEM)
		{
			Coord4D obj = new Coord4D(coord.x, coord.y, coord.z, world.provider.dimensionId);

			List<DynamicNetwork> networks = new ArrayList<>();

			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = obj.getFromSide(side).getTileEntity(world);

				if(tile instanceof ITransmitterTile && TransmissionType.checkTransmissionType(((ITransmitterTile)tile).getTransmitter(), type.getTransmission()))
				{
					networks.add(((ITransmitterTile)tile).getTransmitter().getTransmitterNetwork());
				}
			}

			if(networks.size() > 0)
			{
				/*if(!networks.iterator().next().canMerge(networks))
				{
					return null;
				}*/
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
		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			TransmissionType transmission = TransmitterType.values()[itemstack.getItemDamage()].getTransmission();
			BaseTier tier = TransmitterType.values()[itemstack.getItemDamage()].getTier();
			
			if(transmission == TransmissionType.ENERGY)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(Tier.CableTier.values()[itemstack.getItemDamage()].cableCapacity) + "/t");
			}
			else if(transmission == TransmissionType.FLUID)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.PipeTier.get(tier).pipeCapacity + "mB/t");
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.PipeTier.get(tier).pipePullAmount + "mB/t");
			}
			else if(transmission == TransmissionType.GAS)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.TubeTier.get(tier).tubeCapacity + "mB/t");
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.TubeTier.get(tier).tubePullAmount + "mB/t");
			}
			else if(transmission == TransmissionType.ITEM)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.speed") + ": " + EnumColor.GREY + (Tier.TransporterTier.get(tier).speed/(100/20)) + " m/s");
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.TransporterTier.get(tier).pullAmount*2 + "/s");
			}

			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDetails"));
		}
		else {
			switch(itemstack.getItemDamage())
			{
				case 0: case 1: case 2: case 3:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + "RF " + EnumColor.GREY + "(ThermalExpansion)");
					list.add("- " + EnumColor.PURPLE + "EU " + EnumColor.GREY +  "(IndustrialCraft)");
					list.add("- " + EnumColor.PURPLE + "Joules " + EnumColor.GREY +  "(Mekanism)");
					break;
				}
				case 4: case 5: case 6: case 7:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.fluids") + " " + EnumColor.GREY + "(MinecraftForge)");
					break;
				}
				case 8: case 9: case 10: case 11:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.gasses") + " (Mekanism)");
					break;
				}
				case 12: case 13: case 14: case 15:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.items") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.blocks") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					break;
				}
				case 16:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.items") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.blocks") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.DARK_RED + MekanismUtils.localize("tooltip.restrictiveDesc"));
					break;
				}
				case 17:
				{
					list.add(EnumColor.DARK_GREY + MekanismUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.items") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + MekanismUtils.localize("tooltip.blocks") + " (" + MekanismUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.DARK_RED + MekanismUtils.localize("tooltip.diversionDesc"));
					break;
				}
			}
		}
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List listToAddTo)
	{
		for(TransmitterType type : TransmitterType.values())
		{
			if(type == TransmitterType.HEAT_TRANSMITTER) continue; //TODO
			listToAddTo.add(new ItemStack(item, 1, type.ordinal()));
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
