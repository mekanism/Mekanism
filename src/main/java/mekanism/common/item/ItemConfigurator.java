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
import mekanism.api.util.CapabilityUtils;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;

@InterfaceList({
	@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraft")
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
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		list.add(EnumColor.PINK + LangUtils.localize("gui.state") + ": " + getColor(getState(itemstack)) + getStateDisplay(getState(itemstack)));
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if(!world.isRemote)
		{
			Block block = world.getBlockState(pos).getBlock();
			TileEntity tile = world.getTileEntity(pos);
			
			if(getState(stack).isConfigurating()) //Configurate
			{
				if(tile instanceof ISideConfiguration && ((ISideConfiguration)tile).getConfig().supports(getState(stack).getTransmission()))
				{
					ISideConfiguration config = (ISideConfiguration)tile;
					SideData initial = config.getConfig().getOutput(getState(stack).getTransmission(), side, config.getOrientation());

					if(initial != TileComponentConfig.EMPTY)
					{
						if(!player.isSneaking())
						{
							player.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + getViewModeText(getState(stack).getTransmission()) + ": " + initial.color + initial.localize() + " (" + initial.color.getColoredName() + ")"));
						}
						else {
							if(getEnergy(stack) >= ENERGY_PER_CONFIGURE)
							{
								if(SecurityUtils.canAccess(player, tile))
								{
									setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
									MekanismUtils.incrementOutput(config, getState(stack).getTransmission(), MekanismUtils.getBaseOrientation(side, config.getOrientation()));
									SideData data = config.getConfig().getOutput(getState(stack).getTransmission(), side, config.getOrientation());
									player.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + getToggleModeText(getState(stack).getTransmission()) + ": " + data.color + data.localize() + " (" + data.color.getColoredName() + ")"));
		
									if(config instanceof TileEntityBasicBlock)
									{
										TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)config;
										Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
									}
								}
								else {
									SecurityUtils.displayNoAccess(player);
								}
							}
						}
					}
					
					return EnumActionResult.SUCCESS;
				}
				else if(CapabilityUtils.hasCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side))
				{
					IConfigurable config = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side);

					if(SecurityUtils.canAccess(player, tile))
					{
						if(player.isSneaking())
						{
							return config.onSneakRightClick(player, side);
						}
						else {
							return config.onRightClick(player, side);
						}
					}
					else {
						SecurityUtils.displayNoAccess(player);
						
						return EnumActionResult.SUCCESS;
					}
				}
			}
			else if(getState(stack) == ConfiguratorMode.EMPTY) //Empty
			{
				if(tile instanceof TileEntityContainerBlock)
				{
					IInventory inv = (IInventory)tile;

					if(SecurityUtils.canAccess(player, tile))
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
									EntityItem item = new EntityItem(world, pos.getX() + xRandom, pos.getY() + yRandom, pos.getZ() + zRandom, new ItemStack(slotStack.getItem(), j, slotStack.getItemDamage()));

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

						return EnumActionResult.SUCCESS;
					}
					else {
						SecurityUtils.displayNoAccess(player);
						return EnumActionResult.FAIL;
					}
				}
			}
			else if(getState(stack) == ConfiguratorMode.ROTATE) //Rotate
			{
				EnumFacing[] rotations = block.getValidRotations(world, pos);
				
				if(rotations != null && rotations.length > 0)
				{
					List<EnumFacing> l = Arrays.asList(block.getValidRotations(world, pos));
	
					if(!player.isSneaking() && l.contains(side))
					{
						block.rotateBlock(world, pos, side);
					}
					else if(player.isSneaking() && l.contains(side.getOpposite()))
					{
						block.rotateBlock(world, pos, side.getOpposite());
					}
				}

				return EnumActionResult.SUCCESS;
			}
			else if(getState(stack) == ConfiguratorMode.WRENCH) //Wrench
			{
				return EnumActionResult.PASS;
			}
		}

		return EnumActionResult.PASS;
	}
	
	public String getViewModeText(TransmissionType type)
	{
		String base = LangUtils.localize("tooltip.configurator.viewMode");
		return String.format(base, type.localize().toLowerCase());
	}
	
	public String getToggleModeText(TransmissionType type)
	{
		String base = LangUtils.localize("tooltip.configurator.toggleMode");
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
		ItemDataUtils.setInt(itemstack, "state", state.ordinal());
	}

	public ConfiguratorMode getState(ItemStack itemstack)
	{
		return ConfiguratorMode.values()[ItemDataUtils.getInt(itemstack, "state")];
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	@Method(modid = "BuildCraft")
	public boolean canWrench(EntityPlayer player, BlockPos pos)
	{
		return canUseWrench(player.inventory.getCurrentItem(), player, pos);
	}

	@Override
	@Method(modid = "BuildCraft")
	public void wrenchUsed(EntityPlayer player, BlockPos pos) {}

	@Override
	public boolean canWrench(EntityPlayer player, Entity entity)
	{
		return false;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, Entity entity) {}

	@Override
	public boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos)
	{
		return getState(stack) == ConfiguratorMode.WRENCH;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return getState(stack) == ConfiguratorMode.WRENCH;
	}

	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
		return user instanceof EntityPlayer && canUseWrench(item, (EntityPlayer)user, pos);
	}
	
	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity)
	{
		return user instanceof EntityPlayer && canUseWrench(item, (EntityPlayer)user, null);
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {}
	
	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {}
	
	public static enum ConfiguratorMode
	{
		CONFIGURATE_ITEMS("configurate", "(" + TransmissionType.ITEM.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_FLUIDS("configurate", "(" + TransmissionType.FLUID.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_GASES("configurate", "(" + TransmissionType.GAS.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_ENERGY("configurate", "(" + TransmissionType.ENERGY.localize() + ")", EnumColor.BRIGHT_GREEN, true),
		CONFIGURATE_HEAT("configurate", "(" + TransmissionType.HEAT.localize() + ")", EnumColor.BRIGHT_GREEN, true),
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
			return LangUtils.localize("tooltip.configurator." + name) + " " + info;
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
				case CONFIGURATE_HEAT:
					return TransmissionType.HEAT;
				default:
					return null;
			}
		}
	}
}
