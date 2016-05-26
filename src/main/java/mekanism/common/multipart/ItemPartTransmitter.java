package mekanism.common.multipart;

import java.util.List;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IMetaItem;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
/*
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
*/
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPartTransmitter extends ItemMultiPart implements IMetaItem
{
	public ItemPartTransmitter()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing dir, Vec3d hit, ItemStack stack, EntityPlayer player)
	{
		TransmitterType type = TransmitterType.values()[stack.getItemDamage()];

/*
		if(type.getTransmission() != TransmissionType.ITEM)
		{
			Coord4D obj = new Coord4D(pos, world.provider.getDimensionId());

			List<DynamicNetwork> networks = new ArrayList<DynamicNetwork>();

			for(EnumFacing side : EnumFacing.values())
			{
				TileEntity tile = obj.offset(side).getTileEntity(world);

				if(tile instanceof ITransmitterTile && TransmissionType.checkTransmissionType(((ITransmitterTile)tile).getTransmitter(), type.getTransmission()))
				{
					networks.add(((ITransmitterTile)tile).getTransmitter().getTransmitterNetwork());
				}
			}
		}
*/

		return PartTransmitter.getPartType(TransmitterType.values()[getDamage(stack)]);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			TransmissionType transmission = TransmitterType.values()[itemstack.getItemDamage()].getTransmission();
			BaseTier tier = TransmitterType.values()[itemstack.getItemDamage()].getTier();
			
			if(transmission == TransmissionType.ENERGY)
			{
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(Tier.CableTier.values()[itemstack.getItemDamage()].cableCapacity) + "/t");
			}
			else if(transmission == TransmissionType.FLUID)
			{
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.PipeTier.get(tier).pipeCapacity + "mB/t");
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.PipeTier.get(tier).pipePullAmount + "mB/t");
			}
			else if(transmission == TransmissionType.GAS)
			{
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.TubeTier.get(tier).tubeCapacity + "mB/t");
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.TubeTier.get(tier).tubePullAmount + "mB/t");
			}
			else if(transmission == TransmissionType.ITEM)
			{
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.speed") + ": " + EnumColor.GREY + (Tier.TransporterTier.get(tier).speed/(100/20)) + " m/s");
				list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.TransporterTier.get(tier).pullAmount*2 + "/s");
			}

			list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails"));
		}
		else {
			switch(itemstack.getItemDamage())
			{
				case 0: case 1: case 2: case 3:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + "RF " + EnumColor.GREY + "(ThermalExpansion)");
					list.add("- " + EnumColor.PURPLE + "EU " + EnumColor.GREY +  "(IndustrialCraft)");
					list.add("- " + EnumColor.PURPLE + "Joules " + EnumColor.GREY +  "(Mekanism)");
					break;
				}
				case 4: case 5: case 6: case 7:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.fluids") + " " + EnumColor.GREY + "(MinecraftForge)");
					break;
				}
				case 8: case 9: case 10: case 11:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.gasses") + " (Mekanism)");
					break;
				}
				case 12: case 13: case 14: case 15:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils.localize("tooltip.universal") + ")");
					break;
				}
				case 16:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.DARK_RED + LangUtils.localize("tooltip.restrictiveDesc"));
					break;
				}
				case 17:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils.localize("tooltip.universal") + ")");
					list.add("- " + EnumColor.DARK_RED + LangUtils.localize("tooltip.diversionDesc"));
					break;
				}
				case 18: case 19: case 20: case 21:
				{
					list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
					list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.heat") + " (Mekanism)");
					break;
				}
			}
		}
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> listToAddTo)
	{
		for(TransmitterType type : TransmitterType.values())
		{
			listToAddTo.add(new ItemStack(item, 1, type.ordinal()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return getUnlocalizedName() + "." + TransmitterType.values()[stack.getItemDamage()].getName();
	}

	@Override
	public String getTexture(int meta) 
	{
		return TransmitterType.values()[meta].name().toLowerCase();
	}

	@Override
	public int getVariants() 
	{
		return TransmitterType.values().length;
	}
}
