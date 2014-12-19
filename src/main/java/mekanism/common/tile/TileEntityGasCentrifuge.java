package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.GasCentrifugeRecipe;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityGasCentrifuge extends TileEntityNoisyElectricBlock implements IGasHandler, ITubeConnection
{
	public GasTank inputTank = new GasTank(MAX_GAS);
	public GasTank outputTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int updateDelay;

	public int gasOutput = 16;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public double energyUsage = usage.gasCentrifugeUsage;

	public GasCentrifugeRecipe cachedRecipe;

	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityGasCentrifuge()
	{
		super("machine.centrifuge", "GasCentrifuge", MachineType.GAS_CENTRIFUGE.baseEnergy);
		inventory = new ItemStack[3];
	}

	@Override
	public void onUpdate()
	{
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
			GasCentrifugeRecipe recipe = getRecipe();
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(1, this);

			if(inventory[2] != null && inputTank.getGas() != null)
			{
				inputTank.draw(GasTransmission.addGas(inventory[2], inputTank.getGas()), true);
			}

			if(canOperate(recipe) && getEnergy() >= energyUsage && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - energyUsage);

				operate(recipe);
				markDirty();
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			prevEnergy = getEnergy();

			if(outputTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputTank.getGas().getGas(), Math.min(outputTank.getStored(), gasOutput));

				for(ForgeDirection side : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST})
				{
					TileEntity tileEntity = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);

					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(side.getOpposite(), outputTank.getGas().getGas()))
						{
							outputTank.draw(((IGasHandler)tileEntity).receiveGas(side.getOpposite(), toSend, true), true);
							break;
						}
					}
				}
			}
		}
	}

	public GasCentrifugeRecipe getRecipe()
	{
		GasInput input = getInput();
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getCentrifugeRecipe(getInput());
		}
		return cachedRecipe;
	}

	public GasInput getInput()
	{
		return new GasInput(inputTank.getGas());
	}

	public boolean canOperate(GasCentrifugeRecipe recipe)
	{
		return recipe != null && recipe.canOperate(inputTank, outputTank);
	}

	public void operate(GasCentrifugeRecipe recipe)
	{
		recipe.operate(inputTank, outputTank);
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
		return false;
	}

	public GasTank getTank(ForgeDirection side)
	{
		return side == ForgeDirection.UP ? inputTank : outputTank;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(canReceiveGas(side, stack != null ? stack.getGas() : null))
		{
			return getTank(side).receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		if(canDrawGas(side, null))
		{
			return getTank(side).draw(amount, doTransfer);
		}

		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return getTank(side) == outputTank ? getTank(side).canDraw(type) : false;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return getTank(side) != null && getTank(side) != outputTank ? getTank(side).canReceive(type) : false;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side != ForgeDirection.DOWN;
	}
}
