package mekanism.common.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IMekWrench;
import mekanism.api.Range4D;
import mekanism.common.IInvConfiguration;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|tools")
public class ItemConfigurator extends ItemEnergized implements IMekWrench, IToolWrench
{
	public final int ENERGY_PER_CONFIGURE = 400;
	public final int ENERGY_PER_ITEM_DUMP = 8;

	private Random random = new Random();

	public ItemConfigurator()
	{
		super(60000);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		list.add(EnumColor.PINK + MekanismUtils.localize("gui.state") + ": " + EnumColor.GREY + getStateDisplay(getState(itemstack)));
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			Block block = world.getBlock(x, y, z);
			TileEntity tile = world.getTileEntity(x, y, z);

			if(getState(stack) == 0) //Configurate
			{
				if(tile instanceof IInvConfiguration)
				{
					IInvConfiguration config = (IInvConfiguration)tile;

					if(!player.isSneaking())
					{
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.viewColor") + ": " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName()));
						return true;
					}
					else {
						if(getEnergy(stack) >= ENERGY_PER_CONFIGURE)
						{
							setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
							MekanismUtils.incrementOutput(config, MekanismUtils.getBaseOrientation(side, config.getOrientation()));
							player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleColor") + ": " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName()));

							if(config instanceof TileEntityBasicBlock)
							{
								TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)config;
								Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
							}

							return true;
						}
					}
				}
				else if(tile instanceof IConfigurable)
				{
					IConfigurable config = (IConfigurable)tile;

					if(player.isSneaking())
					{
						return config.onSneakRightClick(player, side);
					}
					else {
						return config.onRightClick(player, side);
					}
				}
			}
			else if(getState(stack) == 1) //Empty
			{
				if(tile instanceof IInventory)
				{
					IInventory inv = (IInventory)tile;

					if(!(inv instanceof TileEntityElectricChest) || (((TileEntityElectricChest)inv).canAccess()))
					{
						for(int i = 0; i < inv.getSizeInventory(); i++)
						{
							ItemStack slotStack = inv.getStackInSlot(i);

							if(slotStack != null)
							{
								if(getEnergy(stack) < ENERGY_PER_ITEM_DUMP)
								{
									break;
								}

								float xRandom = random.nextFloat() * 0.8F + 0.1F;
								float yRandom = random.nextFloat() * 0.8F + 0.1F;
								float zRandom = random.nextFloat() * 0.8F + 0.1F;

								while(slotStack.stackSize > 0)
								{
									int j = random.nextInt(21) + 10;

									if(j > slotStack.stackSize)
									{
										j = slotStack.stackSize;
									}

									slotStack.stackSize -= j;
									EntityItem item = new EntityItem(world, x + xRandom, y + yRandom, z + zRandom, new ItemStack(slotStack.getItem(), j, slotStack.getItemDamage()));

									if(slotStack.hasTagCompound())
									{
										item.getEntityItem().setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
									}

									float k = 0.05F;
									item.motionX = random.nextGaussian() * k;
									item.motionY = random.nextGaussian() * k + 0.2F;
									item.motionZ = random.nextGaussian() * k;
									world.spawnEntityInWorld(item);

									inv.setInventorySlotContents(i, null);
									setEnergy(stack, getEnergy(stack) - ENERGY_PER_ITEM_DUMP);
								}
							}
						}

						return true;
					}
					else {
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.configurator.unauth")));
						return true;
					}
				}
			}
			else if(getState(stack) == 2) //Rotate
			{
				ForgeDirection axis = ForgeDirection.getOrientation(side);
				List<ForgeDirection> l = Arrays.asList(block.getValidRotations(world, x, y, z));

				if(!player.isSneaking() && l.contains(axis))
				{
					block.rotateBlock(world, x, y, z, axis);
				}
				else if(player.isSneaking() && l.contains(axis.getOpposite())) {
					block.rotateBlock(world, x, y, z, axis.getOpposite());
				}

				return true;
			}
			else if(getState(stack) == 3) //Wrench
			{
				return false;
			}
		}

		return false;
	}

	public String getStateDisplay(int state)
	{
		switch(state)
		{
			case 0:
				return MekanismUtils.localize("tooltip.configurator.configurate");
			case 1:
				return MekanismUtils.localize("tooltip.configurator.empty");
			case 2:
				return MekanismUtils.localize("tooltip.configurator.rotate");
			case 3:
				return MekanismUtils.localize("tooltip.configurator.wrench");
		}

		return "unknown";
	}

	public EnumColor getColor(int state)
	{
		switch(state)
		{
			case 0:
				return EnumColor.BRIGHT_GREEN;
			case 1:
				return EnumColor.AQUA;
			case 2:
				return EnumColor.YELLOW;
			case 3:
				return EnumColor.PINK;
		}

		return EnumColor.GREY;
	}

	public void setState(ItemStack itemstack, byte state)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setByte("state", state);
	}

	public byte getState(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return 0;
		}

		byte state = 0;

		if(itemstack.stackTagCompound.getTag("state") != null)
		{
			state = itemstack.stackTagCompound.getByte("state");
		}

		return state;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	@Method(modid = "BuildCraftAPI|tools")
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return canUseWrench(player, x, y, z);
	}

	@Override
	@Method(modid = "BuildCraftAPI|tools")
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {}

	@Override
	public boolean canUseWrench(EntityPlayer player, int x, int y, int z)
	{
		return getState(player.getCurrentEquippedItem()) == 3;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return getState(player.getCurrentEquippedItem()) == 3;
	}
}
