package universalelectricity.core.electricity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.grid.IElectricityNetwork;

public class ElectricalEvent extends Event
{
	/**
	 * Call this to have your TileEntity produce power into the network.
	 * 
	 * @author Calclavia
	 * 
	 */
	@Cancelable
	public static class ElectricityProduceEvent extends ElectricalEvent
	{
		public World world;
		public IElectrical tileEntity;

		public ElectricityProduceEvent(IElectrical tileEntity)
		{
			this.tileEntity = tileEntity;
			this.world = ((TileEntity) this.tileEntity).worldObj;
		}
	}

	public static class NetworkEvent extends ElectricalEvent
	{
		public final IElectricityNetwork network;
		public ElectricityPack electricityPack;
		public TileEntity[] ignoreTiles;

		public NetworkEvent(IElectricityNetwork network, ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			this.network = network;
			this.electricityPack = electricityPack;
			this.ignoreTiles = ignoreTiles;
		}
	}

	/**
	 * Internal Events. These events are fired when something happens in the network.
	 * 
	 * @author Calclavia
	 * 
	 */
	@Cancelable
	public static class ElectricityProductionEvent extends NetworkEvent
	{
		public ElectricityProductionEvent(IElectricityNetwork network, ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			super(network, electricityPack, ignoreTiles);
		}
	}

	public static class ElectricityRequestEvent extends NetworkEvent
	{
		public ElectricityRequestEvent(IElectricityNetwork network, ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			super(network, electricityPack, ignoreTiles);
		}
	}

}
