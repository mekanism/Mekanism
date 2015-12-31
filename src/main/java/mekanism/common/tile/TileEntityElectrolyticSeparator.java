package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig;
import mekanism.api.Range4D;
import mekanism.api.gas.*;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.*;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.ArrayList;
import java.util.List;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IFluidHandler, IComputerIntegration, ITubeConnection, ISustainedData, IGasHandler, IUpgradeTile, IUpgradeInfoHandler, ITankManager, IRedstoneControl, IActiveState
{
	/** This separator's water slot. */
	public FluidTank fluidTank = new FluidTank(24000);

	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 2400;

	/** The amount of oxygen this block is storing. */
	public GasTank leftTank = new GasTank(MAX_GAS);

	/** The amount of hydrogen this block is storing. */
	public GasTank rightTank = new GasTank(MAX_GAS);

	/** How fast this block can output gas. */
	public int output = 256;

	/** The type of gas this block is outputting. */
	public GasMode dumpLeft = GasMode.IDLE;

	/** Type type of gas this block is dumping. */
	public GasMode dumpRight = GasMode.IDLE;
	
	public boolean clientDumpLeft = false;
	public boolean clientDumpRight = false;
	
	public double BASE_ENERGY_USAGE;
	
	public double energyPerTick;

    public int updateDelay;

	public boolean isActive = false;

    public boolean clientActive = false;

    public double prevEnergy;

	public SeparatorRecipe cachedRecipe;
	
	public double clientEnergyUsed;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 4);

    /** This machine's current RedstoneControl type. */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityElectrolyticSeparator()
	{
		super("ElectrolyticSeparator", MachineType.ELECTROLYTIC_SEPARATOR.baseEnergy);
		inventory = new ItemStack[5];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

        if(worldObj.isRemote && updateDelay > 0)
        {
            updateDelay--;

            if(updateDelay == 0 && clientActive != isActive)
            {
                isActive = clientActive;
                MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
            }
        }

		if(!worldObj.isRemote)
		{
            if(updateDelay > 0)
            {
                updateDelay--;

                if(updateDelay == 0 && clientActive != isActive)
                {
                    Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
                }
            }

			ChargeUtils.discharge(3, this);
			
			if(inventory[0] != null)
			{
				if(RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(inventory[0]))
				{
					if(inventory[0].getItem() instanceof IFluidContainerItem)
					{
						fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, inventory[0]), true);
					}
					else {
						FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
	
						if(fluid != null && (fluidTank.getFluid() == null || fluid.isFluidEqual(fluidTank.getFluid()) && fluidTank.getFluid().amount+fluid.amount <= fluidTank.getCapacity()))
						{
							fluidTank.fill(fluid, true);
	
							if(inventory[0].getItem().hasContainerItem(inventory[0]))
							{
								inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
							}
							else {
								inventory[0].stackSize--;
							}
	
							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}

			if(inventory[1] != null && leftTank.getStored() > 0)
			{
				leftTank.draw(GasTransmission.addGas(inventory[1], leftTank.getGas()), true);
				MekanismUtils.saveChunk(this);
			}

			if(inventory[2] != null && rightTank.getStored() > 0)
			{
				rightTank.draw(GasTransmission.addGas(inventory[2], rightTank.getGas()), true);
				MekanismUtils.saveChunk(this);
			}
			
			SeparatorRecipe recipe = getRecipe();

			if(canOperate(recipe) && getEnergy() >= energyPerTick && MekanismUtils.canFunction(this))
			{
                setActive(true);

				boolean update = BASE_ENERGY_USAGE != recipe.energyUsage;
				
				BASE_ENERGY_USAGE = recipe.energyUsage;
				
				if(update)
				{
					recalculateUpgradables(Upgrade.ENERGY);
				}
				
				int operations = operate(recipe);
				double prev = getEnergy();
				
				setEnergy(getEnergy() - energyPerTick*operations);
				clientEnergyUsed = prev-getEnergy();
			}
			else {
                if(prevEnergy >= getEnergy())
                {
                    setActive(false);
                }
			}
			
			int dumpAmount = 8*(int)Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
			
			boolean dumpedLeft = false;
			boolean dumpedRight = false;

			if(leftTank.getGas() != null)
			{
				if(dumpLeft != GasMode.DUMPING)
				{
					GasStack toSend = new GasStack(leftTank.getGas().getGas(), Math.min(leftTank.getStored(), output));

					TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);

					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), leftTank.getGas().getGas()))
						{
							leftTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getLeft(facing).getOpposite(), toSend, true), true);
						}
					}
				}
				else if(dumpLeft == GasMode.DUMPING)
				{
					leftTank.draw(dumpAmount, true);
					
					dumpedLeft = true;
				}
				
				if(dumpLeft == GasMode.DUMPING_EXCESS && leftTank.getNeeded() < output)
				{
					leftTank.draw(output-leftTank.getNeeded(), true);
					
					dumpedLeft = true;
				}
			}

			if(rightTank.getGas() != null)
			{
				if(dumpRight != GasMode.DUMPING)
				{
					GasStack toSend = new GasStack(rightTank.getGas().getGas(), Math.min(rightTank.getStored(), output));

					TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), rightTank.getGas().getGas()))
						{
							rightTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend, true), true);
						}
					}
				}
				else if(dumpRight == GasMode.DUMPING)
				{
					rightTank.draw(dumpAmount, true);
					
					dumpedRight = true;
				}
				
				if(dumpRight == GasMode.DUMPING_EXCESS && rightTank.getNeeded() < output)
				{
					rightTank.draw(output-rightTank.getNeeded(), true);
					
					dumpedRight = true;
				}
			}
			
			if(clientDumpLeft != dumpedLeft || clientDumpRight != dumpedRight)
			{
				clientDumpLeft = dumpedLeft;
				clientDumpRight = dumpedRight;
				
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}

            prevEnergy = getEnergy();
		}
		else {
			if(clientDumpLeft)
			{
				spawnParticle(0);
			}
			
			if(clientDumpRight)
			{
				spawnParticle(1);
			}
		}
	}
	
	public int getUpgradedUsage(SeparatorRecipe recipe)
	{
		int possibleProcess = 0;
		
		if(leftTank.getGasType() == recipe.recipeOutput.leftGas.getGas())
		{
			possibleProcess = leftTank.getNeeded()/recipe.recipeOutput.leftGas.amount;
			possibleProcess = Math.min(rightTank.getNeeded()/recipe.recipeOutput.rightGas.amount, possibleProcess);
		}
		else {
			possibleProcess = leftTank.getNeeded()/recipe.recipeOutput.rightGas.amount;
			possibleProcess = Math.min(rightTank.getNeeded()/recipe.recipeOutput.leftGas.amount, possibleProcess);
		}
		
		possibleProcess = Math.min((int)Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), possibleProcess);
		possibleProcess = Math.min((int)(getEnergy()/energyPerTick), possibleProcess);
		
		return Math.min(fluidTank.getFluidAmount()/recipe.recipeInput.ingredient.amount, possibleProcess);
	}

	public SeparatorRecipe getRecipe()
	{
		FluidInput input = getInput();
		
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getElectrolyticSeparatorRecipe(getInput());
		}
		
		return cachedRecipe;
	}

	public FluidInput getInput()
	{
		return new FluidInput(fluidTank.getFluid());
	}

	public boolean canOperate(SeparatorRecipe recipe)
	{
		return recipe != null && recipe.canOperate(fluidTank, leftTank, rightTank);
	}

	public int operate(SeparatorRecipe recipe)
	{
		int operations = getUpgradedUsage(recipe);
		
		recipe.operate(fluidTank, leftTank, rightTank, operations);
		
		return operations;
	}

	public boolean canFill(ChemicalPairOutput gases)
	{
		return (leftTank.canReceive(gases.leftGas.getGas()) && leftTank.getNeeded() >= gases.leftGas.amount
				&& rightTank.canReceive(gases.rightGas.getGas()) && rightTank.getNeeded() >= gases.rightGas.amount);
	}

	public void spawnParticle(int type)
	{
		if(type == 0)
		{
			ForgeDirection side = ForgeDirection.getOrientation(facing);

			double x = xCoord + (side.offsetX == 0 ? 0.5 : Math.max(side.offsetX, 0));
			double z = zCoord + (side.offsetZ == 0 ? 0.5 : Math.max(side.offsetZ, 0));

			worldObj.spawnParticle("smoke", x, yCoord + 0.5, z, 0.0D, 0.0D, 0.0D);
		}
		else if(type == 1)
		{
			switch(facing)
			{
				case 3:
					worldObj.spawnParticle("smoke", xCoord+0.9, yCoord+1, zCoord+0.75, 0.0D, 0.0D, 0.0D);
					break;
				case 4:
					worldObj.spawnParticle("smoke", xCoord+0.25, yCoord+1, zCoord+0.9, 0.0D, 0.0D, 0.0D);
					break;
				case 2:
					worldObj.spawnParticle("smoke", xCoord+0.1, yCoord+1, zCoord+0.25, 0.0D, 0.0D, 0.0D);
					break;
				case 5:
					worldObj.spawnParticle("smoke", xCoord+0.75, yCoord+1, zCoord+0.1, 0.0D, 0.0D, 0.0D);
					break;
			}
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 1 || slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
					((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack);
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(itemstack);
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogen"));
		}
		else if(slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen"));
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing))
		{
			return new int[] {3};
		}
		else if(side == facing || ForgeDirection.getOrientation(side) == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return new int[] {1, 2};
		}

		return InventoryUtils.EMPTY;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			byte type = dataStream.readByte();

			if(type == 0)
			{
				dumpLeft = GasMode.values()[dumpLeft.ordinal() == GasMode.values().length-1 ? 0 : dumpLeft.ordinal()+1];
			}
			else if(type == 1)
			{
				dumpRight = GasMode.values()[dumpRight.ordinal() == GasMode.values().length-1 ? 0 : dumpRight.ordinal()+1];
			}

			return;
		}

		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}

		if(dataStream.readBoolean())
		{
			leftTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			leftTank.setGas(null);
		}

		if(dataStream.readBoolean())
		{
			rightTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			rightTank.setGas(null);
		}

        controlType = RedstoneControl.values()[dataStream.readInt()];
		dumpLeft = GasMode.values()[dataStream.readInt()];
		dumpRight = GasMode.values()[dataStream.readInt()];
		clientDumpLeft = dataStream.readBoolean();
		clientDumpRight = dataStream.readBoolean();
		clientActive = dataStream.readBoolean();
		clientEnergyUsed = dataStream.readDouble();

        if(updateDelay == 0 && clientActive != isActive)
        {
            updateDelay = MekanismConfig.general.UPDATE_DELAY;
            isActive = clientActive;
            MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
        }
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().getFluid().getID());
			data.add(fluidTank.getFluidAmount());
		}
		else {
			data.add(false);
		}

		if(leftTank.getGas() != null)
		{
			data.add(true);
			data.add(leftTank.getGas().getGas().getID());
			data.add(leftTank.getStored());
		}
		else {
			data.add(false);
		}

		if(rightTank.getGas() != null)
		{
			data.add(true);
			data.add(rightTank.getGas().getGas().getID());
			data.add(rightTank.getStored());
		}
		else {
			data.add(false);
		}

        data.add(controlType.ordinal());
		data.add(dumpLeft.ordinal());
		data.add(dumpRight.ordinal());
		data.add(clientDumpLeft);
		data.add(clientDumpRight);
		data.add(isActive);
		data.add(clientEnergyUsed);

		return data;
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}

        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		leftTank.read(nbtTags.getCompoundTag("leftTank"));
		rightTank.read(nbtTags.getCompoundTag("rightTank"));

		dumpLeft = GasMode.values()[nbtTags.getInteger("dumpLeft")];
		dumpRight = GasMode.values()[nbtTags.getInteger("dumpRight")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}

        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setTag("leftTank", leftTank.write(new NBTTagCompound()));
		nbtTags.setTag("rightTank", rightTank.write(new NBTTagCompound()));

		nbtTags.setInteger("dumpLeft", dumpLeft.ordinal());
		nbtTags.setInteger("dumpRight", dumpRight.ordinal());
	}

	private static final String[] methods = new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded", "getHydrogen", "getHydrogenNeeded", "getOxygen", "getOxygenNeeded"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {BASE_MAX_ENERGY};
			case 3:
				return new Object[] {(BASE_MAX_ENERGY -electricityStored)};
			case 4:
				return new Object[] {fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0};
			case 5:
				return new Object[] {fluidTank.getFluid() != null ? (fluidTank.getCapacity()- fluidTank.getFluid().amount) : 0};
			case 6:
				return new Object[] {leftTank.getStored()};
			case 7:
				return new Object[] {leftTank.getNeeded()};
			case 8:
				return new Object[] {rightTank.getStored()};
			case 9:
				return new Object[] {rightTank.getNeeded()};
			default:
				throw new NoSuchMethodException();
		}
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(fluidTank.getFluid() != null)
		{
			itemStack.stackTagCompound.setTag("fluidTank", fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
		
		if(leftTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("leftTank", leftTank.getGas().write(new NBTTagCompound()));
		}
		
		if(rightTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("rightTank", rightTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("fluidTank")));
		leftTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("leftTank")));
		rightTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("rightTank")));
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] {fluidTank.getInfo()};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank.draw(amount, doTransfer);
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank.draw(amount, doTransfer);
		}

		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank.getGas() != null && leftTank.getGas().getGas() == type;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank.getGas() != null && rightTank.getGas().getGas() == type;
		}

		return false;
	}

    @Override
    public RedstoneControl getControlType()
    {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type)
    {
        controlType = type;
        MekanismUtils.saveChunk(this);
    }

    @Override
    public boolean canPulse()
    {
        return false;
    }

    @Override
    public void setActive(boolean active)
    {
        isActive = active;

        if(clientActive != active && updateDelay == 0)
        {
            Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));

            updateDelay = 10;
            clientActive = active;
        }
    }

    @Override
    public boolean getActive()
    {
        return isActive;
    }

    @Override
    public boolean renderUpdate()
    {
        return false;
    }

    @Override
    public boolean lightUpdate()
    {
        return true;
    }
	
	@Override
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}
	
	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case ENERGY:
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
				energyPerTick = BASE_ENERGY_USAGE; //Don't scale energy usage.
			default:
				break;
		}
	}
	
	@Override
	public List<String> getInfo(Upgrade upgrade) 
	{
		return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {fluidTank, leftTank, rightTank};
	}
}
