package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import codechicken.lib.vec.Vector3;

public class PartPressurizedTube extends PartTransmitter<GasNetwork> implements IGasHandler
{
	public static TransmitterIcons tubeIcons = new TransmitterIcons(1, 1);

	public float currentScale;

	public GasStack cacheGas;
	public GasStack lastWrite;

	@Override
	public void update()
	{
		if(!world().isRemote)
		{
			if(cacheGas != null)
			{
				if(getTransmitterNetwork().gasStored == null)
				{
					getTransmitterNetwork().gasStored = cacheGas;
				}
				else {
					getTransmitterNetwork().gasStored.amount += cacheGas.amount;
				}

				cacheGas = null;
			}

			if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0)
			{
				int last = lastWrite != null ? lastWrite.amount : 0;

				if(last != getSaveShare())
				{
					MekanismUtils.saveChunk(tile());
				}
			}
		}
		else {
			float targetScale = getTransmitterNetwork().gasScale;

			if(Math.abs(currentScale - targetScale) > 0.01)
			{
				currentScale = (9 * currentScale + targetScale) / 10;
			}
		}

		super.update();
	}

	private int getSaveShare()
	{
		if(getTransmitterNetwork().gasStored != null)
		{
			int remain = getTransmitterNetwork().gasStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().gasStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<GasNetwork>)tile()))
			{
				toSave += remain;
			}

			return toSave;
		}

		return 0;
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.PRESSURIZED_TUBE;
	}

	@Override
	public void preSingleMerge(GasNetwork network)
	{
		if(cacheGas != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = cacheGas;
			}
			else {
				network.gasStored.amount += cacheGas.amount;
			}

			cacheGas = null;
		}
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote)
		{
			if(lastWrite != null)
			{
				if(getTransmitterNetwork().gasStored != null)
				{
					getTransmitterNetwork().gasStored.amount -= lastWrite.amount;

					if(getTransmitterNetwork().gasStored.amount <= 0)
					{
						getTransmitterNetwork().gasStored = null;
					}
				}
			}
		}

		super.onChunkUnload();
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		if(nbtTags.hasKey("cacheGas"))
		{
			cacheGas = GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas"));
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0 && getTransmitterNetwork(false).gasStored != null)
		{
			int remain = getTransmitterNetwork().gasStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().gasStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<GasNetwork>)tile()))
			{
				toSave += remain;
			}

			if(toSave > 0)
			{
				GasStack stack = new GasStack(getTransmitterNetwork().gasStored.getGas(), toSave);

				lastWrite = stack;
				nbtTags.setTag("cacheGas", stack.write(new NBTTagCompound()));
			}
		}
	}

	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube";
	}

	public static void registerIcons(IIconRegister register)
	{
		tubeIcons.registerCenterIcons(register, new String[] {"PressurizedTube"});
		tubeIcons.registerSideIcons(register, new String[] {"TransmitterSideSmall"});
	}

	@Override
	public IIcon getCenterIcon()
	{
		return tubeIcons.getCenterIcon(0);
	}

	@Override
	public IIcon getSideIcon()
	{
		return tubeIcons.getSideIcon(0);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return GasTransmission.canConnect(tile, side);
	}

	@Override
	public GasNetwork createNetworkFromSingleTransmitter(IGridTransmitter<GasNetwork> transmitter)
	{
		return new GasNetwork(transmitter);
	}

	@Override
	public GasNetwork createNetworkByMergingSet(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeededInfo();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlowInfo();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public int getCapacity()
	{
		return 256;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack) 
	{
		if(getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL)
		{
			return getTransmitterNetwork().emit(stack);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount) 
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type) 
	{
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}
}
