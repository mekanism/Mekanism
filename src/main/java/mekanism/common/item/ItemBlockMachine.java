package mekanism.common.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.List;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IElectricChest;
import mekanism.common.base.IFactory;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * 1:9: Seismic Vibrator
 * 1:10: Pressurized Reaction Chamber
 * 1:11: Portable Tank
 * 1:12: Fluidic Plenisher
 * 1:13: Laser
 * 1:14: Laser Amplifier
 * 1:15: Laser Tractor Beam
 * 2:0: Entangled Block
 * 2:1: Solar Neutron Activator
 * 2:2: Ambient Accumulator
 * 2:3: Oredictionificator
 * @author AidanBrady
 *
 */
@InterfaceList({
		@Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHCore"),
		@Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = "IC2")
})
public class ItemBlockMachine extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, IFactory, ISustainedInventory, ISustainedTank, IElectricChest, IEnergyContainerItem, IFluidContainerItem
{
	public Block metaBlock;

	public ItemBlockMachine(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
		setNoRepair();
		setMaxStackSize(1);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		if(MachineType.get(itemstack) != null)
		{
			return getUnlocalizedName() + "." + MachineType.get(itemstack).name;
		}

		return "null";
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack itemstack)
	{
		MachineType type = MachineType.get(itemstack);
		
		if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
		{
			String tier = type == MachineType.BASIC_FACTORY ? BaseTier.BASIC.getLocalizedName() : (type == MachineType.ADVANCED_FACTORY ? 
					BaseTier.ADVANCED.getLocalizedName() : BaseTier.ELITE.getLocalizedName());
			
			return tier + " " + RecipeType.values()[getRecipeType(itemstack)].getLocalizedName() + " " + super.getItemStackDisplayName(itemstack);
		}
		
		return super.getItemStackDisplayName(itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		MachineType type = MachineType.get(itemstack);

		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			if(type == MachineType.PORTABLE_TANK)
			{
				FluidStack fluidStack = getFluidStack(itemstack);
				
				if(fluidStack != null)
				{
					list.add(EnumColor.PINK + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}

			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDetails") + ".");
			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " and " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.modeSwitchKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDesc") + ".");
		}
		else if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey))
		{
			if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.recipeType") + ": " + EnumColor.GREY + RecipeType.values()[getRecipeType(itemstack)].getLocalizedName());
			}

			if(type == MachineType.ELECTRIC_CHEST)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.auth") + ": " + EnumColor.GREY + LangUtils.transYesNo(getAuthenticated(itemstack)));
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.locked") + ": " + EnumColor.GREY + LangUtils.transYesNo(getLocked(itemstack)));
			}
			
			if(type == MachineType.PORTABLE_TANK)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.portableTank.bucketMode") + ": " + EnumColor.GREY + LangUtils.transYesNo(getBucketMode(itemstack)));
			}

			if(type.isElectric)
			{
				list.add(EnumColor.BRIGHT_GREEN + MekanismUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
			}

			if(hasTank(itemstack) && type != MachineType.PORTABLE_TANK)
			{
				FluidStack fluidStack = getFluidStack(itemstack);
				
				if(fluidStack != null)
				{
					list.add(EnumColor.PINK + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}
			
			if(type != MachineType.CHARGEPAD && type != MachineType.LOGISTICAL_SORTER)
			{
				list.add(EnumColor.AQUA + MekanismUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
			}

			if(type.supportsUpgrades && itemstack.stackTagCompound != null && itemstack.stackTagCompound.hasKey("upgrades"))
			{
				Map<Upgrade, Integer> upgrades = Upgrade.buildMap(itemstack.stackTagCompound);
				
				for(Map.Entry<Upgrade, Integer> entry : upgrades.entrySet())
				{
					list.add(entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ? ": " + EnumColor.GREY + "x" + entry.getValue(): ""));
				}
			}
		}
		else {
			list.addAll(MekanismUtils.splitLines(type.getDescription()));
		}
	}
	
	@Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		MachineType type = MachineType.get(stack);
		
		if(type == MachineType.PORTABLE_TANK && getBucketMode(stack))
		{
			return false;
		}
		
		return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = true;
		
		MachineType type = MachineType.get(stack);

		if(type == MachineType.DIGITAL_MINER)
		{
			for(int xPos = x-1; xPos <= x+1; xPos++)
			{
				for(int yPos = y; yPos <= y+1; yPos++)
				{
					for(int zPos = z-1; zPos <= z+1; zPos++)
					{
						Block b = world.getBlock(xPos, yPos, zPos);

						if(yPos > 255 || !b.isReplaceable(world, xPos, yPos, zPos))
						{
							place = false;
						}
					}
				}
			}
		}
		else if(type == MachineType.SOLAR_NEUTRON_ACTIVATOR)
		{
			if(y+1 > 255 || !world.getBlock(x, y+1, z).isReplaceable(world, x, y+1, z))
			{
				place = false;
			}
		}

		if(place && super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

			if(tileEntity instanceof IUpgradeTile)
			{
				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("upgrades"))
				{
					((IUpgradeTile)tileEntity).getComponent().read(stack.stackTagCompound);
				}
			}

			if(tileEntity instanceof ISideConfiguration)
			{
				ISideConfiguration config = (ISideConfiguration)tileEntity;

				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("sideDataStored"))
				{
					config.getConfig().read(stack.stackTagCompound);
				}
			}
			
			if(tileEntity instanceof ISustainedData)
			{
				if(stack.stackTagCompound != null)
				{
					((ISustainedData)tileEntity).readSustainedData(stack);
				}
			}

			if(tileEntity instanceof IRedstoneControl)
			{
				if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("controlType"))
				{
					((IRedstoneControl)tileEntity).setControlType(RedstoneControl.values()[stack.stackTagCompound.getInteger("controlType")]);
				}
			}

			if(tileEntity instanceof TileEntityFactory)
			{
				TileEntityFactory factory = (TileEntityFactory)tileEntity;
				RecipeType recipeType = RecipeType.values()[getRecipeType(stack)];
				factory.recipeType = recipeType;
				factory.upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
				factory.secondaryEnergyPerTick = factory.getSecondaryEnergyPerTick(recipeType);
				world.notifyBlocksOfNeighborChange(x, y, z, tileEntity.getBlockType());
			}

			if(tileEntity instanceof ISustainedTank)
			{
				if(hasTank(stack) && getFluidStack(stack) != null)
				{
					((ISustainedTank)tileEntity).setFluidStack(getFluidStack(stack));
				}
			}

			if(tileEntity instanceof TileEntityElectricChest)
			{
				((TileEntityElectricChest)tileEntity).authenticated = getAuthenticated(stack);
				((TileEntityElectricChest)tileEntity).locked = getLocked(stack);
				((TileEntityElectricChest)tileEntity).password = getPassword(stack);
			}

			if(tileEntity instanceof ISustainedInventory)
			{
				((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
			}

			if(tileEntity instanceof TileEntityElectricBlock)
			{
				((TileEntityElectricBlock)tileEntity).electricityStored = getEnergy(stack);
			}

			return true;
		}

		return false;
	}

	@Override
	@Method(modid = "IC2")
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return false;
	}

	@Override
	@Method(modid = "IC2")
	public double getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	@Method(modid = "IC2")
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	@Method(modid = "IC2")
	public double getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		MachineType type = MachineType.get(itemstack);
		
		if(type == MachineType.ELECTRIC_CHEST)
		{
			if(world != null && !world.isRemote)
			{
				InventoryElectricChest inv = new InventoryElectricChest(itemstack);

				if(inv.getStackInSlot(54) != null && getEnergy(itemstack) < getMaxEnergy(itemstack))
				{
					if(inv.getStackInSlot(54).getItem() instanceof IEnergizedItem)
					{
						setEnergy(itemstack, getEnergy(itemstack) + EnergizedItemManager.discharge(inv.getStackInSlot(54), getMaxEnergy(itemstack) - getEnergy(itemstack)));
					}
					else if(MekanismUtils.useIC2() && inv.getStackInSlot(54).getItem() instanceof IElectricItem)
					{
						IElectricItem item = (IElectricItem)inv.getStackInSlot(54).getItem();

						if(item.canProvideEnergy(inv.getStackInSlot(54)))
						{
							double gain = ElectricItem.manager.discharge(inv.getStackInSlot(54), (int)((getMaxEnergy(itemstack) - getEnergy(itemstack))* general.TO_IC2), 3, false, true, false)* general.FROM_IC2;
							setEnergy(itemstack, getEnergy(itemstack) + gain);
						}
					}
					else if(MekanismUtils.useRF() && inv.getStackInSlot(54).getItem() instanceof IEnergyContainerItem)
					{
						ItemStack itemStack = inv.getStackInSlot(54);
						IEnergyContainerItem item = (IEnergyContainerItem)inv.getStackInSlot(54).getItem();

						int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
						int toTransfer = (int)Math.round(Math.min(itemEnergy, ((getMaxEnergy(itemstack) - getEnergy(itemstack))* general.TO_TE)));

						setEnergy(itemstack, getEnergy(itemstack) + (item.extractEnergy(itemStack, toTransfer, false)* general.FROM_TE));
					}
					else if(inv.getStackInSlot(54).getItem() == Items.redstone && getEnergy(itemstack)+ general.ENERGY_PER_REDSTONE <= getMaxEnergy(itemstack))
					{
						setEnergy(itemstack, getEnergy(itemstack) + general.ENERGY_PER_REDSTONE);
						inv.getStackInSlot(54).stackSize--;

						if(inv.getStackInSlot(54).stackSize <= 0)
						{
							inv.setInventorySlotContents(54, null);
						}
					}

					inv.write();
				}
			}
		}
		else if(type == MachineType.PORTABLE_TANK)
		{
			if(world != null && !world.isRemote)
			{
				float targetScale = (float)(getFluidStack(itemstack) != null ? getFluidStack(itemstack).amount : 0)/TileEntityPortableTank.MAX_FLUID;
	
				if(Math.abs(getPrevScale(itemstack) - targetScale) > 0.01)
				{
					setPrevScale(itemstack, (9*getPrevScale(itemstack) + targetScale)/10);
				}
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem)
	{
		onUpdate(entityItem.getEntityItem(), null, entityItem, 0, false);

		return false;
	}

    public boolean tryPlaceContainedLiquid(World world, ItemStack itemstack, int x, int y, int z)
    {
        if(getFluidStack(itemstack) == null || !getFluidStack(itemstack).getFluid().canBePlacedInWorld())
        {
            return false;
        }
        else {
            Material material = world.getBlock(x, y, z).getMaterial();
            boolean flag = !material.isSolid();

            if(!world.isAirBlock(x, y, z) && !flag)
            {
                return false;
            }
            else {
                if(world.provider.isHellWorld && getFluidStack(itemstack).getFluid() == FluidRegistry.WATER)
                {
                    world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                    for(int l = 0; l < 8; l++)
                    {
                        world.spawnParticle("largesmoke", x + Math.random(), y + Math.random(), z + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                }
                else {
                    if(!world.isRemote && flag && !material.isLiquid())
                    {
                        world.func_147480_a(x, y, z, true);
                    }
                    
                    world.setBlock(x, y, z, MekanismUtils.getFlowingBlock(getFluidStack(itemstack).getFluid()), 0, 3);
                }

                return true;
            }
        }
    }

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		MachineType type = MachineType.get(itemstack);
		
		if(MachineType.get(itemstack) == MachineType.ELECTRIC_CHEST)
		{
			if(!world.isRemote)
			{
				if(!getAuthenticated(itemstack))
				{
					Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, false, false, 2, 0, null, null), (EntityPlayerMP)entityplayer);
				}
				else if(getLocked(itemstack) && getEnergy(itemstack) > 0)
				{
					Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, false, false, 1, 0, null, null), (EntityPlayerMP)entityplayer);
				}
				else {
					InventoryElectricChest inventory = new InventoryElectricChest(entityplayer);
					MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, null, inventory, false);
				}
			}
		}
		else if(type == MachineType.PORTABLE_TANK && getBucketMode(itemstack))
    	{
	        MovingObjectPosition pos = getMovingObjectPositionFromPlayer(world, entityplayer, !entityplayer.isSneaking());
	        
	        if(pos == null)
	        {
	            return itemstack;
	        }
	        else {
	            if(pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
	            {
	            	Coord4D coord = new Coord4D(pos.blockX, pos.blockY, pos.blockZ, world.provider.dimensionId);
	
	                if(!world.canMineBlock(entityplayer, coord.xCoord, coord.yCoord, coord.zCoord))
	                {
	                    return itemstack;
	                }
	
	                if(!entityplayer.isSneaking())
	                {
	                    if(!entityplayer.canPlayerEdit(coord.xCoord, coord.yCoord, coord.zCoord, pos.sideHit, itemstack))
	                    {
	                        return itemstack;
	                    }
	                    
	                    FluidStack fluid = MekanismUtils.getFluid(world, coord.xCoord, coord.yCoord, coord.zCoord, false);
	                    
	                    if(fluid != null && (getFluidStack(itemstack) == null || getFluidStack(itemstack).isFluidEqual(fluid)))
	                    {
	                		int needed = TileEntityPortableTank.MAX_FLUID-(getFluidStack(itemstack) != null ? getFluidStack(itemstack).amount : 0);
	                		
	                		if(fluid.amount > needed)
	                		{
	                			return itemstack;
	                		}
	                		
	                		if(getFluidStack(itemstack) == null)
	                		{
	                			setFluidStack(fluid, itemstack);
	                		}
	                		else {
	                			FluidStack newStack = getFluidStack(itemstack);
	                			newStack.amount += fluid.amount;
	                			setFluidStack(newStack, itemstack);
	                		}
	                		
	                		world.setBlockToAir(coord.xCoord, coord.yCoord, coord.zCoord);
	                    }
	                }
	                else {
	            		FluidStack stored = getFluidStack(itemstack);
	        			
	        			if(stored == null || stored.amount < FluidContainerRegistry.BUCKET_VOLUME)
	        			{
	        				return itemstack;
	        			}
	        			
	        			Coord4D trans = coord.getFromSide(ForgeDirection.getOrientation(pos.sideHit));

	                    if(!entityplayer.canPlayerEdit(trans.xCoord, trans.yCoord, trans.zCoord, pos.sideHit, itemstack))
	                    {
	                        return itemstack;
	                    }

	                    if(tryPlaceContainedLiquid(world, itemstack, trans.xCoord, trans.yCoord, trans.zCoord) && !entityplayer.capabilities.isCreativeMode)
	                    {
	                    	FluidStack newStack = stored.copy();
	                    	newStack.amount -= FluidContainerRegistry.BUCKET_VOLUME;
	                    	
	                    	setFluidStack(newStack.amount > 0 ? newStack : null, itemstack);
	                    }
	                }
	            }
	
	            return itemstack;
	        }
    	}

		return itemstack;
	}

	@Override
	public int getRecipeType(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getInteger("recipeType");
	}

	@Override
	public void setRecipeType(int type, ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("recipeType", type);
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			
			itemStack.stackTagCompound.setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return null;
			}
			
			return itemStack.stackTagCompound.getTagList("Items", NBT.TAG_COMPOUND);
		}

		return null;
	}

	@Override
	public void setFluidStack(FluidStack fluidStack, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			
			if(fluidStack == null || fluidStack.amount == 0 || fluidStack.fluidID == 0)
			{
				itemStack.stackTagCompound.removeTag("fluidTank");
			}
			else {
				itemStack.stackTagCompound.setTag("fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
			}
		}
	}

	@Override
	public FluidStack getFluidStack(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return null;
			}

			if(itemStack.stackTagCompound.hasKey("fluidTank"))
			{
				return FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("fluidTank"));
			}
		}

		return null;
	}

	@Override
	public boolean hasTank(Object... data)
	{
		if(!(data[0] instanceof ItemStack) || !(((ItemStack)data[0]).getItem() instanceof ISustainedTank))
		{
			return false;
		}
		
		MachineType type = MachineType.get((ItemStack)data[0]);
		
		return type == MachineType.ELECTRIC_PUMP || type == MachineType.PORTABLE_TANK || type == MachineType.FLUIDIC_PLENISHER;
	}

	@Override
	public void setAuthenticated(ItemStack itemStack, boolean auth)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("authenticated", auth);
	}

	@Override
	public boolean getAuthenticated(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("authenticated");
	}

	@Override
	public void setPassword(ItemStack itemStack, String pass)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setString("password", pass);
	}

	@Override
	public String getPassword(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return "";
		}

		return itemStack.stackTagCompound.getString("password");
	}

	@Override
	public void setLocked(ItemStack itemStack, boolean locked)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("locked", locked);
	}

	@Override
	public boolean getLocked(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("locked");
	}

	@Override
	public void setOpen(ItemStack itemStack, boolean open)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("open", open);
	}

	@Override
	public boolean getOpen(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("open");
	}

	public void setPrevScale(ItemStack itemStack, float prevLidAngle)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("prevScale", prevLidAngle);
	}

	public float getPrevScale(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0.0F;
		}

		return itemStack.stackTagCompound.getFloat("prevScale");
	}
	
	public void setBucketMode(ItemStack itemStack, boolean bucketMode)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("bucketMode", bucketMode);
	}
	
	public boolean getBucketMode(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("bucketMode");
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null || !MachineType.get(itemStack).isElectric)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getDouble("electricity");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(!MachineType.get(itemStack).isElectric)
		{
			return;
		}
		
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		return MekanismUtils.getMaxEnergy(itemStack, MachineType.get(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage()).baseEnergy);
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return MachineType.get(itemStack).isElectric;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canReceive(theItem))
		{
			double energyNeeded = getMaxEnergy(theItem)-getEnergy(theItem);
			double toReceive = Math.min(energy* general.FROM_TE, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive* general.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy* general.FROM_TE), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend* general.TO_TE);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)(getEnergy(theItem) * general.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)(getMaxEnergy(theItem) * general.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific(ItemStack itemStack)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2")
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
	
	@Override
	@Method(modid = "IC2")
	public Item getChargedItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	@Method(modid = "IC2")
	public Item getEmptyItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return getFluidStack(container);
	}

	@Override
	public int getCapacity(ItemStack container) 
	{
		return TileEntityPortableTank.MAX_FLUID;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) 
	{
		if(MachineType.get(container) == MachineType.PORTABLE_TANK && resource != null)
		{
			FluidStack stored = getFluidStack(container);
			int toFill;

			if(stored != null && stored.getFluid() != resource.getFluid())
			{
				return 0;
			}
			
			if(stored == null)
			{
				toFill = Math.min(resource.amount, TileEntityPortableTank.MAX_FLUID);
			}
			else {
				toFill = Math.min(resource.amount, TileEntityPortableTank.MAX_FLUID-stored.amount);
			}
			
			if(doFill)
			{
				int fillAmount = toFill + (stored == null ? 0 : stored.amount);
				setFluidStack(new FluidStack(resource.getFluid(), (stored != null ? stored.amount : 0)+toFill), container);
			}
			
			return toFill;
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) 
	{
		if(MachineType.get(container) == MachineType.PORTABLE_TANK)
		{
			FluidStack stored = getFluidStack(container);
			
			if(stored != null)
			{
				FluidStack toDrain = new FluidStack(stored.getFluid(), Math.min(stored.amount, maxDrain));
				
				if(doDrain)
				{
					stored.amount -= toDrain.amount;
					setFluidStack(stored.amount > 0 ? stored : null, container);
				}
				
				return toDrain;
			}
		}
		
		return null;
	}
}
