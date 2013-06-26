package mekanism.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import mekanism.api.EnumGas;
import mekanism.api.GasTransmission;
import mekanism.api.IPressurizedTube;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * The actual protocol gas goes through when it is transferred via Pressurized Tubes.
 * @author AidanBrady
 *
 */
public class GasClientUpdate
{
	/** List of iterated tubes, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedTubes = new ArrayList<TileEntity>();
	
	/** Pointer tube of this calculation */
	public TileEntity pointer;
	
	/** Type of gas to distribute */
	public EnumGas transferType;
	
	/**
	 * GasTransferProtocol -- a calculation used to distribute gasses through a tube network.
	 * @param head - pointer tile entity
	 * @param orig - original outputter
	 * @param type - type of gas being transferred
	 * @param amount - amount of gas to distribute
	 */
	public GasClientUpdate(TileEntity head, EnumGas type)
	{
		pointer = head;
		transferType = type;
	}
	
	public void loopThrough(TileEntity tile)
	{
		if(!iteratedTubes.contains(tile))
		{
			iteratedTubes.add(tile);
		}
		
		TileEntity[] tubes = GasTransmission.getConnectedTubes(tile);
		
		for(TileEntity tube : tubes)
		{
			if(tube != null)
			{
				if(!iteratedTubes.contains(tube))
				{
					loopThrough(tube);
				}
			}
		}
	}
	
	public void clientUpdate()
	{
		loopThrough(pointer);
		
		for(TileEntity tileEntity : iteratedTubes)
		{
			if(tileEntity instanceof IPressurizedTube)
			{
				((IPressurizedTube)tileEntity).onTransfer(transferType);
			}
		}
	}
}
