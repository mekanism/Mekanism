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
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
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
import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

@InterfaceList({
		@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraft"),
		@Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore")
})
public class ItemConfigurator extends ItemEnergized implements IMekWrench, IToolWrench, IToolHammer
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
		list.add(EnumColor.PINK + MekanismUtils.localize("gui.state") + ": " + getColor(getState(itemstack)) + getStateDisplay(getState(itemstack)));
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			Block block = world.getBlock(x, y, z);
			TileEntity tile = world.getTileEntity(x, y, z);

			if(getState(stack).isConfigurating()) //Configurate
			{
				if(tile instanceof ISideConfiguration && ((ISideConfiguration)tile).getConfig().supports(getState(stack).getTransmission()))
				{
					ISideConfiguration config = (ISideConfiguration)tile;

					if(!player.isSneaking())
					{
						SideData data = config.getConfig().getOutput(getState(stack).getTransmission(), side, config.getOrientation());
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + getViewModeText(getState(stack).getTransmission()) + ": " + data.color + data.localize() + " (" + data.color.getName() + ")"));
						return true;
					}
					else {
						if(getEnergy(stack) >= ENERGY_PER_CONFIGURE)
						{
							setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
							MekanismUtils.incrementOutput(config, getState(stack).getTransmission(), MekanismUtils.getBaseOrientation(side, config.getOrientation()));
							SideData data = config.getConfig().getOutput(getState(stack).getTransmission(), side, config.getOrientation());
							player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + getToggleModeText(getState(stack).getTransmission()) + ": " + data.color + data.localize() + " (" + data.color.getName() + ")"));

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
			else if(getState(stack) == ConfiguratorMode.EMPTY) //Empty
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
			else if(getState(stack) == ConfiguratorMode.ROTATE) //Rotate
			{
				ForgeDirection axis = ForgeDirection.getOrientation(side);
				List<ForgeDirection> l = Arrays.asList(block.getValidRotations(world, x, y, z));

				if(!player.isSneaking() && l.contains(axis))
				{
					block.rotateBlock(world, x, y, z, axis);
				}
				else if(player.isSneaking() && l.contains(axis.getOpposite()))
				{
					block.rotateBlock(world, x, y, z, axis.getOpposite());
				}

				return true;
			}
			else if(getState(stack) == ConfiguratorMode.WRENCH) //Wrench
			{
				return false;
			}
		}

		return false;
	}
	
	public String getViewModeText(TransmissionType type)
	{
		String base = MekanismUtils.localize("tooltip.configurator.viewMode");
		return String.format(base, type.localize().toLowerCase());
	}
	
	public String getToggleModeText(TransmissionType type)
	{
		String base = MekanismUtils.localize("tooltip.configurator.toggleMode");
		return String.format(base, type.localize());
	}

	public String getStateDisplay(ConfiguratorMode mode)
	{
		return mode.getName();
	}

	public EnumColor getColor(ConfiguratorMode mode)
	{
		return mode.getColor();
	}

	public void setState(ItemStack itemstack, ConfiguratorMode state)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setInteger("state", state.ordinal());
	}

	public ConfiguratorMode getState(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return ConfiguratorMode.CONFIGURATE_ITEMS;
		}

		if(itemstack.stackTagCompound.getTag("state") != null)
		{
			return ConfiguratorMode.values()[itemstack.stackTagCompound.getInteger("state")];
		}

		return ConfiguratorMode.CONFIGURATE_ITEMS;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	@Method(modid = "BuildCraft")
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return canUseWrench(player, x, y, z);
	}

	@Override
	@Method(modid = "BuildCraft")
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {}

	@Override
	public boolean canUseWrench(EntityPlayer player, int x, int y, int z)
	{
		return getState(player.getCurrentEquippedItem()) == ConfiguratorMode.WRENCH;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return getState(player.getCurrentEquippedItem()) == ConfiguratorMode.WRENCH;
	}

	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, int x, int y, int z) 
	{
		return user instanceof EntityPlayer && canUseWrench((EntityPlayer)user, x, y, z);
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, int x, int y, int z) {}
	
	public static enum ConfiguratorMode
	{
		CONFIGURATE_ITEMS("configurate", "(" + TransmissionType.ITEM.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_FLUIDS("configurate", "(" + TransmissionType.FLUID.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_GASES("configurate", "(" + TransmissionType.GAS.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_ENERGY("configurate", "(" + TransmissionType.ENERGY.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		EMPTY("empty", "", EnumColor.DARK_RED, false),
		ROTATE("rotate", "", EnumColor.YELLOW, false),
		WRENCH("wrench", "", EnumColor.PINK, false);
		
		private String name;
		private String info;
		private EnumColor color;
		private boolean configurating;
		
		private ConfiguratorMode(String s, String s1, EnumColor c, boolean b)
		{
			name = s;
			info = s1;
			color = c;
			configurating = b;
		}
		
		public String getName()
		{
			return MekanismUtils.localize("tooltip.configurator." + name) + " " + info;
		}
		
		public EnumColor getColor()
		{
			return color;
		}
		
		public boolean isConfigurating()
		{
			return configurating;
		}
		
		public TransmissionType getTransmission()
		{
			switch(this)
			{
				case CONFIGURATE_ITEMS:
					return TransmissionType.ITEM;
				case CONFIGURATE_FLUIDS:
					return TransmissionType.FLUID;
				case CONFIGURATE_GASES:
					return TransmissionType.GAS;
				case CONFIGURATE_ENERGY:
					return TransmissionType.ENERGY;
				default:
					return null;
			}
		}
	}
}
