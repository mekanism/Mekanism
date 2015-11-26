package mekanism.common.multipart;

import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.gas.*;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.Tier;
import mekanism.common.Tier.TubeTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collection;

public class PartPressurizedTube extends PartTransmitter<IGasHandler, GasNetwork> implements IGasHandler
{
	public Tier.TubeTier tier = Tier.TubeTier.BASIC;
	
	public static TransmitterIcons tubeIcons = new TransmitterIcons(4, 8);

	public float currentScale;

	public GasTank buffer = new GasTank(getCapacity());

	public GasStack lastWrite;
	
	public PartPressurizedTube(Tier.TubeTier tubeTier)
	{
		super();
		tier = tubeTier;
	}

	@Override
	public void update()
	{
		if(!world().isRemote)
        {
            updateShare();

			IGasHandler[] connectedAcceptors = GasTransmission.getConnectedAcceptors(tile());

			for(ForgeDirection side : getConnections(ConnectionType.PULL))
			{
				if(connectedAcceptors[side.ordinal()] != null)
				{
					IGasHandler container = connectedAcceptors[side.ordinal()];

					if(container != null)
					{
						GasStack received = container.drawGas(side.getOpposite(), tier.tubePullAmount, false);

						if(received != null && received.amount != 0)
						{
							container.drawGas(side.getOpposite(), takeGas(received, true), true);
						}
					}
				}
			}

		}
		else {
			float targetScale = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().gasScale : (float)buffer.getStored()/(float)buffer.getMaxGas();

			if(Math.abs(currentScale - targetScale) > 0.01)
			{
				currentScale = (9 * currentScale + targetScale) / 10;
			}
		}

		super.update();
	}

    @Override
    public void updateShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
        {
            GasStack last = getSaveShare();

            if((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getGas() == last.getGas())) || (last == null && lastWrite != null))
            {
                lastWrite = last;
                MekanismUtils.saveChunk(tile());
            }
        }
    }

	private GasStack getSaveShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null)
		{
			int remain = getTransmitter().getTransmitterNetwork().buffer.amount%getTransmitter().getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitter().getTransmitterNetwork().buffer.amount/getTransmitter().getTransmitterNetwork().transmitters.size();

			if(getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter()))
			{
				toSave += remain;
			}

            return new GasStack(getTransmitter().getTransmitterNetwork().buffer.getGas(), toSave);
		}

		return null;
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote && getTransmitter().hasTransmitterNetwork())
		{
			if(lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null)
			{
				getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

				if(getTransmitter().getTransmitterNetwork().buffer.amount <= 0)
				{
					getTransmitter().getTransmitterNetwork().buffer = null;
				}
			}
		}

		super.onChunkUnload();
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);
		
		tier = TubeTier.values()[nbtTags.getInteger("tier")];
		buffer.setMaxGas(getCapacity());

		if(nbtTags.hasKey("cacheGas"))
		{
			buffer.setGas(GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas")));
		}
        else {
            buffer.setGas(null);
        }
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

        if(lastWrite != null && lastWrite.amount > 0)
        {
            nbtTags.setTag("cacheGas", lastWrite.write(new NBTTagCompound()));
        }
        else {
            nbtTags.removeTag("cacheGas");
		}

        nbtTags.setInteger("tier", tier.ordinal());
	}

	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube_" + tier.name().toLowerCase();
	}

	public static void registerIcons(IIconRegister register)
	{
		tubeIcons.registerCenterIcons(register, new String[] {"PressurizedTubeBasic", "PressurizedTubeAdvanced", "PressurizedTubeElite", "PressurizedTubeUltimate"});
		tubeIcons.registerSideIcons(register, new String[] {"SmallTransmitterVerticalBasic", "SmallTransmitterVerticalAdvanced", "SmallTransmitterVerticalElite", "SmallTransmitterVerticalUltimate",
				"SmallTransmitterHorizontalBasic", "SmallTransmitterHorizontalAdvanced", "SmallTransmitterHorizontalElite", "SmallTransmitterHorizontalUltimate"});
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return tubeIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return tubeIcons.getSideIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return tubeIcons.getSideIcon(4+tier.ordinal());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return GasTransmission.canConnect(tile, side);
	}

	@Override
	public GasNetwork createNewNetwork()
	{
		return new GasNetwork();
	}

	@Override
	public GasNetwork createNetworkByMerging(Collection<GasNetwork> networks)
	{
		return new GasNetwork(networks);
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
		return tier.tubeCapacity;
	}

	@Override
	public GasStack getBuffer()
	{
		return buffer == null ? null : buffer.getGas();
	}

	@Override
	public void takeShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null && lastWrite != null)
        {
            getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
            buffer.setGas(lastWrite);
        }
    }

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL)
		{
			return takeGas(stack, doTransfer);
		}
		
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
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	public int takeGas(GasStack gasStack, boolean doEmit)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().emit(gasStack, doEmit);
		}
		else {
			return buffer.receive(gasStack, doEmit);
		}
	}

	@Override
	public IGasHandler getCachedAcceptor(ForgeDirection side)
	{
		if(cachedAcceptors[side.ordinal()] instanceof IGasHandler)
		{
			return super.getCachedAcceptor(side);
		}
		
		return null;
	}
}
