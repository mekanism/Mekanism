package mekanism.common.item;

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
import mekanism.common.Upgrade;
import mekanism.common.base.IElectricChest;
import mekanism.common.base.IFactory;
import mekanism.common.base.IInvConfiguration;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
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
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.api.energy.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

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
 * @author AidanBrady
 *
 */
@InterfaceList({
		@Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy"),
		@Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = "IC2API")
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
		if(MachineBlockType.get(itemstack) != null)
		{
			return getUnlocalizedName() + "." + MachineBlockType.get(itemstack).name;
		}

		return "null";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		MachineBlockType type = MachineBlockType.get(itemstack);

		if(!MekKeyHandler.isPressed(MekanismKeyHandler.sneakKey))
		{
			if(type == MachineBlockType.PORTABLE_TANK)
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
		else if(!MekKeyHandler.isPressed(MekanismKeyHandler.modeSwitchKey))
		{
			if(type == MachineBlockType.BASIC_FACTORY || type == MachineBlockType.ADVANCED_FACTORY || type == MachineBlockType.ELITE_FACTORY)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.recipeType") + ": " + EnumColor.GREY + RecipeType.values()[getRecipeType(itemstack)].getName());
			}

			if(type == MachineBlockType.ELECTRIC_CHEST)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.auth") + ": " + EnumColor.GREY + LangUtils.transYesNo(getAuthenticated(itemstack)));
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.locked") + ": " + EnumColor.GREY + LangUtils.transYesNo(getLocked(itemstack)));
			}
			
			if(type == MachineBlockType.PORTABLE_TANK)
			{
				list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.portableTank.bucketMode") + ": " + EnumColor.GREY + LangUtils.transYesNo(getBucketMode(itemstack)));
			}

			if(type.isElectric)
			{
				list.add(EnumColor.BRIGHT_GREEN + MekanismUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergyStored(itemstack)));
			}

			if(hasTank(itemstack) && type != MachineBlockType.PORTABLE_TANK)
			{
				FluidStack fluidStack = getFluidStack(itemstack);
				if(fluidStack != null)
				{
					list.add(EnumColor.PINK + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}
			
			if(type != MachineBlockType.CHARGEPAD && type != MachineBlockType.LOGISTICAL_SORTER)
			{
				list.add(EnumColor.AQUA + MekanismUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
			}

			if(type.supportsUpgrades && itemstack.getTagCompound() != null && itemstack.getTagCompound().hasKey("upgrades"))
			{
				Map<Upgrade, Integer> upgrades = Upgrade.buildMap(itemstack.getTagCompound());
				
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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		MachineBlockType type = MachineBlockType.get(stack);

		if(type == MachineBlockType.PORTABLE_TANK && getBucketMode(stack))
		{
			return false;
		}
		
		return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
    }

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		boolean place = true;

		MachineBlockType type = MachineBlockType.get(stack);

		if(type == MachineBlockType.DIGITAL_MINER)
		{
			for(int xPos = -1; xPos <= +1; xPos++)
			{
				for(int yPos = 0; yPos <= +1; yPos++)
				{
					for(int zPos = -1; zPos <= +1; zPos++)
					{
						Block b = world.getBlockState(new BlockPos(xPos, yPos, zPos)).getBlock();

						if(yPos > 255)
						{
							place = false;
						}

						if(!b.isReplaceable(world, pos.add(xPos, yPos, zPos)))
						{
							return false;
						}
					}
				}
			}
		}

		if(place && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

			if(tileEntity instanceof IUpgradeTile)
			{
				if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("upgrades"))
				{
					((IUpgradeTile)tileEntity).getComponent().read(stack.getTagCompound());
				}
			}

			if(tileEntity instanceof IInvConfiguration)
			{
				IInvConfiguration config = (IInvConfiguration)tileEntity;

				if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("hasSideData"))
				{
					config.getEjector().setEjecting(stack.getTagCompound().getBoolean("ejecting"));

					for(int i = 0; i < 6; i++)
					{
						config.getConfiguration()[i] = stack.getTagCompound().getByte("config"+i);
					}
				}
			}
			
			if(tileEntity instanceof ISustainedData)
			{
				if(stack.getTagCompound() != null)
				{
					((ISustainedData)tileEntity).readSustainedData(stack);
				}
			}

			if(tileEntity instanceof IRedstoneControl)
			{
				if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("controlType"))
				{
					((IRedstoneControl)tileEntity).setControlType(RedstoneControl.values()[stack.getTagCompound().getInteger("controlType")]);
				}
			}

			if(tileEntity instanceof TileEntityFactory)
			{
				((TileEntityFactory)tileEntity).recipeType = RecipeType.values()[getRecipeType(stack)];
				world.notifyNeighborsOfStateChange(pos, tileEntity.getBlockType());
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
	@Method(modid = "IC2API")
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return false;
	}

	@Override
	@Method(modid = "IC2API")
	public double getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	@Method(modid = "IC2API")
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	@Method(modid = "IC2API")
	public double getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		MachineBlockType type = MachineBlockType.get(itemstack);
		
		if(type == MachineBlockType.ELECTRIC_CHEST)
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
		else if(type == MachineBlockType.PORTABLE_TANK)
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

    public boolean tryPlaceContainedLiquid(World world, ItemStack itemstack, BlockPos pos)
    {
        if(getFluidStack(itemstack) == null || !getFluidStack(itemstack).getFluid().canBePlacedInWorld())
        {
            return false;
        }
        else {
            Material material = world.getBlockState(pos).getBlock().getMaterial();
            boolean flag = !material.isSolid();

            if(!world.isAirBlock(pos) && !flag)
            {
                return false;
            }
            else {
                if(world.provider.doesWaterVaporize() && getFluidStack(itemstack).getFluid() == FluidRegistry.WATER)
                {
                    world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                    for(int l = 0; l < 8; l++)
                    {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                }
                else {
                    if(!world.isRemote && flag && !material.isLiquid())
                    {
                        world.destroyBlock(pos, true);
                    }
                    
                    world.setBlockState(pos, MekanismUtils.getFlowingBlock(getFluidStack(itemstack).getFluid()).getStateFromMeta(0), 3);
                }

                return true;
            }
        }
    }

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		MachineBlockType type = MachineBlockType.get(itemstack);
		
		if(MachineBlockType.get(itemstack) == MachineBlockType.ELECTRIC_CHEST)
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
		else if(type == MachineBlockType.PORTABLE_TANK && getBucketMode(itemstack))
    	{
	        MovingObjectPosition pos = getMovingObjectPositionFromPlayer(world, entityplayer, !entityplayer.isSneaking());
	        
	        if(pos == null)
	        {
	            return itemstack;
	        }
	        else {
	            if(pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
	            {
	            	Coord4D coord = new Coord4D(pos.getBlockPos(), world.provider.getDimensionId());
	
	                if(!world.isBlockModifiable(entityplayer, coord))
	                {
	                    return itemstack;
	                }
	
	                if(!entityplayer.isSneaking())
	                {
	                    if(!entityplayer.canPlayerEdit(coord, pos.sideHit, itemstack))
	                    {
	                        return itemstack;
	                    }
	                    
	                    FluidStack fluid = MekanismUtils.getFluid(world, coord);
	                    
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
	                		
	                		world.setBlockToAir(coord);
	                    }
	                }
	                else {
	            		FluidStack stored = getFluidStack(itemstack);
	        			
	        			if(stored == null || stored.amount < FluidContainerRegistry.BUCKET_VOLUME)
	        			{
	        				return itemstack;
	        			}
	        			
	        			Coord4D trans = coord.offset(pos.sideHit);

	                    if(!entityplayer.canPlayerEdit(trans, pos.sideHit, itemstack))
	                    {
	                        return itemstack;
	                    }

	                    if(tryPlaceContainedLiquid(world, itemstack, trans) && !entityplayer.capabilities.isCreativeMode)
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
		if(itemStack.getTagCompound() == null)
		{
			return 0;
		}

		return itemStack.getTagCompound().getInteger("recipeType");
	}

	@Override
	public void setRecipeType(int type, ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setInteger("recipeType", type);
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			
			itemStack.getTagCompound().setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				return null;
			}
			
			return itemStack.getTagCompound().getTagList("Items", NBT.TAG_COMPOUND);
		}

		return null;
	}

	@Override
	public void setFluidStack(FluidStack fluidStack, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			
			if(fluidStack == null || fluidStack.amount == 0 || fluidStack.fluidID == 0)
			{
				itemStack.getTagCompound().removeTag("fluidTank");
			}
			else {
				itemStack.getTagCompound().setTag("fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
			}
		}
	}

	@Override
	public FluidStack getFluidStack(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				return null;
			}

			if(itemStack.getTagCompound().hasKey("fluidTank"))
			{
				return FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound().getCompoundTag("fluidTank"));
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

		MachineBlockType type = MachineBlockType.get((ItemStack)data[0]);
		
		return type == MachineBlockType.ELECTRIC_PUMP || type == MachineBlockType.PORTABLE_TANK || type == MachineBlockType.FLUIDIC_PLENISHER;
	}

	@Override
	public void setAuthenticated(ItemStack itemStack, boolean auth)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setBoolean("authenticated", auth);
	}

	@Override
	public boolean getAuthenticated(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return false;
		}

		return itemStack.getTagCompound().getBoolean("authenticated");
	}

	@Override
	public void setPassword(ItemStack itemStack, String pass)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setString("password", pass);
	}

	@Override
	public String getPassword(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return "";
		}

		return itemStack.getTagCompound().getString("password");
	}

	@Override
	public void setLocked(ItemStack itemStack, boolean locked)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setBoolean("locked", locked);
	}

	@Override
	public boolean getLocked(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return false;
		}

		return itemStack.getTagCompound().getBoolean("locked");
	}

	@Override
	public void setOpen(ItemStack itemStack, boolean open)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setBoolean("open", open);
	}

	@Override
	public boolean getOpen(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return false;
		}

		return itemStack.getTagCompound().getBoolean("open");
	}

	public void setPrevScale(ItemStack itemStack, float prevLidAngle)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setFloat("prevScale", prevLidAngle);
	}

	public float getPrevScale(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return 0.0F;
		}

		return itemStack.getTagCompound().getFloat("prevScale");
	}
	
	public void setBucketMode(ItemStack itemStack, boolean bucketMode)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setBoolean("bucketMode", bucketMode);
	}
	
	public boolean getBucketMode(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return false;
		}

		return itemStack.getTagCompound().getBoolean("bucketMode");
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null || !MachineBlockType.get(itemStack).isElectric)
		{
			return 0;
		}

		return itemStack.getTagCompound().getDouble("electricity");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(!MachineBlockType.get(itemStack).isElectric)
		{
			return;
		}
		
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.getTagCompound().setDouble("electricity", electricityStored);
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		return MekanismUtils.getMaxEnergy(itemStack, MachineBlockType.get(itemStack).baseEnergy);
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return MachineBlockType.get(itemStack).isElectric;
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
		return (int)(getEnergy(theItem)* general.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)(getMaxEnergy(theItem)* general.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific(ItemStack itemStack)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2API")
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
	
	@Override
	@Method(modid = "IC2API")
	public Item getChargedItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	@Method(modid = "IC2API")
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
		if(MachineBlockType.get(container) == MachineBlockType.PORTABLE_TANK && resource != null)
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
				setFluidStack(new FluidStack(resource.getFluid(), fillAmount), container);
			}
			
			return toFill;
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) 
	{
		if(MachineBlockType.get(container) == MachineBlockType.PORTABLE_TANK)
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
