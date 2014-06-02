package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.FluidNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PacketTransmitterUpdate extends MekanismPacket
{
	public PacketType packetType;

	public TileEntity tileEntity;

	public double power;

	public GasStack gasStack;
	public boolean didGasTransfer;

	public FluidStack fluidStack;
	public boolean didFluidTransfer;

	public PacketTransmitterUpdate(PacketType type, TileEntity tile, Object... data)
	{
		packetType = type;
		tileEntity = tile;

		switch(packetType)
		{
			case ENERGY:
				power = (Double)data[0];
				break;
			case GAS:
				gasStack = (GasStack)data[0];
				didGasTransfer = (Boolean)data[1];
				break;
			case FLUID:
				fluidStack = (FluidStack)data[0];
				didFluidTransfer = (Boolean)data[1];
				break;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(packetType.ordinal());

		dataStream.writeInt(tileEntity.xCoord);
		dataStream.writeInt(tileEntity.yCoord);
		dataStream.writeInt(tileEntity.zCoord);
		dataStream.writeInt(tileEntity.getWorldObj().provider.dimensionId);

		switch(packetType)
		{
			case ENERGY:
				dataStream.writeDouble(power);
				break;
			case GAS:
				dataStream.writeInt(gasStack != null ? gasStack.getGas().getID() : -1);
				dataStream.writeInt(gasStack != null ? gasStack.amount : 0);
				dataStream.writeBoolean(didGasTransfer);
				break;
			case FLUID:
				dataStream.writeInt(fluidStack != null ? fluidStack.getFluid().getID() : -1);
				dataStream.writeInt(fluidStack != null ? fluidStack.amount : 0);
				dataStream.writeBoolean(didFluidTransfer);
				break;
		}
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		packetType = PacketType.values()[dataStream.readInt()];

		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		if(packetType == PacketType.UPDATE)
		{
			TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);

			if(tileEntity instanceof IGridTransmitter)
			{
				((IGridTransmitter)tileEntity).refreshTransmitterNetwork();
			}
		}
		else if(packetType == PacketType.ENERGY)
		{
			double powerLevel = dataStream.readDouble();

			TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);

			if(tileEntity != null)
			{
				((IGridTransmitter<EnergyNetwork>)tileEntity).getTransmitterNetwork().clientEnergyScale = powerLevel;
			}
		}
		else if(packetType == PacketType.GAS)
		{
			TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);

			Gas gasType = GasRegistry.getGas(dataStream.readInt());
			int amount = dataStream.readInt();
			GasStack stack = null;
			didGasTransfer = dataStream.readBoolean();

			if(gasType != null)
			{
				stack = new GasStack(gasType, amount);
			}

			if(tileEntity != null)
			{
				GasNetwork net = ((IGridTransmitter<GasNetwork>)tileEntity).getTransmitterNetwork();

				if(gasType != null)
				{
					net.refGas = gasType;
				}

				net.gasStored = stack;
				net.didTransfer = didGasTransfer;
			}
		}
		else if(packetType == PacketType.FLUID)
		{
			TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);

			int type = dataStream.readInt();
			Fluid fluidType = type != -1 ? FluidRegistry.getFluid(type) : null;
			int amount = dataStream.readInt();
			FluidStack stack = null;
			didFluidTransfer = dataStream.readBoolean();

			if(fluidType != null)
			{
				stack = new FluidStack(fluidType, amount);
			}

			if(tileEntity != null)
			{
				FluidNetwork net = ((IGridTransmitter<FluidNetwork>)tileEntity).getTransmitterNetwork();

				if(fluidType != null)
				{
					net.refFluid = fluidType;
				}

				net.fluidStored = stack;
				net.didTransfer = didFluidTransfer;
				net.fluidScale = net.getScale();
			}
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}

	public static enum PacketType
	{
		UPDATE,
		ENERGY,
		GAS,
		FLUID
	}
}