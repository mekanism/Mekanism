package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityAmbientAccumulator extends TileEntityContainerBlock implements IGasHandler, ITubeConnection
{
	public GasTank collectedGas = new GasTank(1000);

	public static Map<Integer, String> dimensionGases = new HashMap<Integer, String>();

	public static Random gasRand = new Random();

	static
	{
		dimensionGases.put(-1, "sulfurDioxideGas");
		dimensionGases.put(+0, "oxygen");
		dimensionGases.put(+1, "tritium");
	}

	public TileEntityAmbientAccumulator()
	{
		super("AmbientAccumulator");
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			Gas gasToCollect = GasRegistry.getGas(dimensionGases.get(worldObj.provider.dimensionId));

			if(gasToCollect != null && gasRand.nextDouble() < 0.05)
			{
				collectedGas.receive(new GasStack(gasToCollect, 1), true);
			}
		}
	}


	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		return collectedGas.draw(amount, doTransfer);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return type == collectedGas.getGasType();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		if(collectedGas.getGasType() != null)
		{
			data.add(collectedGas.getGasType().getID());
			data.add(collectedGas.getStored());
		} else
		{
			data.add(-1);
			data.add(0);
		}
		return data;
	}

	@Override
	public void handlePacketData(ByteBuf data)
	{
		int gasID = data.readInt();
		if(gasID < 0)
		{
			collectedGas.setGas(null);
		} else
		{
			collectedGas.setGas(new GasStack(gasID, data.readInt()));
		}
	}
}
