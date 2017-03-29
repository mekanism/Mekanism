package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.ITierItem;
import mekanism.common.multipart.BlockStateTransmitter.TransmitterType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTransmitter extends ItemBlock implements ITierItem
{
	public Block metaBlock;
	
	public ItemBlockTransmitter(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState state)
	{
		boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

		if(place)
		{
			TileEntitySidedPipe tileEntity = (TileEntitySidedPipe)world.getTileEntity(pos);
			tileEntity.setBaseTier(getBaseTier(stack));
			
			if(!world.isRemote)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
			}
		}

		return place;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			TransmissionType transmission = TransmitterType.values()[itemstack.getItemDamage()].getTransmission();
			BaseTier tier = getBaseTier(itemstack);
			
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
	public String getUnlocalizedName(ItemStack stack)
	{
		TransmitterType type = TransmitterType.get(stack.getItemDamage());
		String name = type.getUnlocalizedName();
		
		if(type.hasTiers())
		{
			BaseTier tier = getBaseTier(stack);
			name = tier.getSimpleName() + name;
		}
		
		return getUnlocalizedName() + "." + name;
	}
	
	@Override
	public BaseTier getBaseTier(ItemStack itemstack)
	{
		if(!itemstack.hasTagCompound())
		{
			return BaseTier.BASIC;
		}

		return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
	}

	@Override
	public void setBaseTier(ItemStack itemstack, BaseTier tier)
	{
		if(!itemstack.hasTagCompound())
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("tier", tier.ordinal());
	}
}
