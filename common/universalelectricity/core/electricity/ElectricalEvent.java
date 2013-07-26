package universalelectricity.core.electricity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;
import universalelectricity.core.block.IElectrical;

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

	/**
	 * Internal Events
	 * 
	 * @author Calclavia
	 * 
	 */
	@Cancelable
	public static class ElectricityProductionEvent extends ElectricalEvent
	{
		public ElectricityPack electricityPack;
		public TileEntity[] ignoreTiles;

		public ElectricityProductionEvent(ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			this.electricityPack = electricityPack;
			this.ignoreTiles = ignoreTiles;
		}
	}

	public static class ElectricityRequestEvent extends ElectricalEvent
	{
		public ElectricityPack electricityPack;
		public TileEntity[] ignoreTiles;

		public ElectricityRequestEvent(ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			this.electricityPack = electricityPack;
			this.ignoreTiles = ignoreTiles;
		}
	}

}
