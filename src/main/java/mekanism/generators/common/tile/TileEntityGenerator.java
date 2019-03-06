package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.TileNetworkList;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityNoisyBlock;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.block.states.BlockStateGenerator;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityGenerator extends TileEntityNoisyBlock implements IComputerIntegration, IActiveState, IRedstoneControl, ISecurityTile
{
	/** Output per tick this generator can transfer. */
	public double output;

	/** Whether or not this block is in it's active state. */
	public boolean isActive;

	/** The client's current active state. */
	public boolean clientActive;

	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

	/**
	 * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
	 * @param name - full name of this generator
	 * @param maxEnergy - how much energy this generator can store
	 */
	public TileEntityGenerator(String soundPath, String name, double maxEnergy, double out)
	{
		super("gen." + soundPath, name, maxEnergy);

		output = out;
		isActive = false;
		controlType = RedstoneControl.DISABLED;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(world.isRemote && updateDelay > 0)
		{
			updateDelay--;

			if(updateDelay == 0 && clientActive != isActive)
			{
				isActive = clientActive;
				MekanismUtils.updateBlock(world, getPos());
			}
		}

		if(!world.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					clientActive = isActive;
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(this)));
				}
			}
			
			if(!world.isRemote && general.destroyDisabledBlocks)
			{
				GeneratorType type = BlockStateGenerator.GeneratorType.get(getBlockType(), getBlockMetadata());
				
				if(type != null && !type.isEnabled())
				{
					Mekanism.logger.info("[Mekanism] Destroying generator of type '" + type.blockName + "' at coords " + Coord4D.get(this) + " as according to config.");
					world.setBlockToAir(getPos());
					return;
				}
			}

			if(MekanismUtils.canFunction(this))
			{
				CableUtils.emit(this);
			}
		}
	}

	@Override
	public double getMaxOutput()
	{
		return output;
	}

	@Override
	public boolean sideIsConsumer(EnumFacing side) 
	{
		return false;
	}

	@Override
	public boolean sideIsOutput(EnumFacing side) {
		return side == facing;
	}

	/**
	 * Whether or not this generator can operate.
	 * @return if the generator can operate
	 */
	public abstract boolean canOperate();

	@Override
	public boolean getActive()
	{
		return isActive;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(this)));

			updateDelay = general.UPDATE_DELAY;
			clientActive = active;
		}
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			clientActive = dataStream.readBoolean();
			controlType = RedstoneControl.values()[dataStream.readInt()];
	
			if(updateDelay == 0 && clientActive != isActive)
			{
				updateDelay = general.UPDATE_DELAY;
				isActive = clientActive;
				MekanismUtils.updateBlock(world, getPos());
			}
		}
	}

	@Override
	public TileNetworkList getNetworkedData(TileNetworkList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(controlType.ordinal());

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		isActive = nbtTags.getBoolean("isActive");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());
		
		return nbtTags;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
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
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
	}
}
