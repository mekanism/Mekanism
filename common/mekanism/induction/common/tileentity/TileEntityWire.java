package mekanism.induction.common.tileentity;

import java.util.ArrayList;

import mekanism.common.ITileNetwork;
import mekanism.induction.common.wire.EnumWireMaterial;
import mekanism.induction.common.wire.IInsulatedMaterial;
import mekanism.induction.common.wire.IInsulation;
import mekanism.induction.common.wire.IWireMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.compatibility.TileEntityUniversalConductor;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityWire extends TileEntityUniversalConductor implements ITileNetwork, IInsulatedMaterial
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public boolean isInsulated = false;

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(this).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(worldObj);
		
		if(connectTile instanceof IWireMaterial)
		{
			IWireMaterial wireTile = (IWireMaterial) connectTile;

			if(wireTile.getMaterial() != getMaterial())
			{
				return false;
			}
		}

		if(isInsulated() && connectTile instanceof IInsulation)
		{
			IInsulation insulatedTile = (IInsulation) connectTile;

			if((insulatedTile.isInsulated() && insulatedTile.getInsulationColor() != getInsulationColor() && getInsulationColor() != DEFAULT_COLOR && insulatedTile.getInsulationColor() != DEFAULT_COLOR))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void refresh()
	{
		if(!worldObj.isRemote)
		{
			adjacentConnections = null;

			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(canConnect(side.getOpposite()))
				{
					TileEntity tileEntity = VectorHelper.getConnectorFromSide(worldObj, new Vector3(this), side);

					if(tileEntity != null)
					{
						if(tileEntity instanceof INetworkProvider)
						{
							getNetwork().merge(((INetworkProvider)tileEntity).getNetwork());
						}
					}
				}
			}

			getNetwork().refresh();
		}
	}

	@Override
	public float getResistance()
	{
		return getMaterial().resistance;
	}

	@Override
	public float getCurrentCapacity()
	{
		return getMaterial().maxAmps;
	}

	@Override
	public EnumWireMaterial getMaterial()
	{
		return EnumWireMaterial.values()[getTypeID()];
	}

	public int getTypeID()
	{
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}

	/**
	 * @param dyeID
	 */
	public void setDye(int dye)
	{
		dyeID = dye;
		refresh();
		
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType().blockID);
	}

	public void setInsulated()
	{
		isInsulated = true;
		refresh();
		
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType().blockID);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(isInsulated);
		data.add(dyeID);
		
		return data;
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		try {
			isInsulated = dataStream.readBoolean();
			dyeID = dataStream.readInt();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		dyeID = nbt.getInteger("dyeID");
		isInsulated = nbt.getBoolean("isInsulated");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("dyeID", dyeID);
		nbt.setBoolean("isInsulated", isInsulated);
	}
	
	@Override
	public boolean isInsulated()
	{
		return isInsulated;
	}

	@Override
	public void setInsulated(boolean insulated)
	{
		if(insulated && !isInsulated())
		{
			setInsulated();
		}
	}

	@Override
	public int getInsulationColor()
	{
		return dyeID;
	}

	@Override
	public void setInsulationColor(int dyeID)
	{
		setDye(dyeID);
	}
}
