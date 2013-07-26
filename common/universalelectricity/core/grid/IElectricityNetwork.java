package universalelectricity.core.grid;

import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.electricity.ElectricityPack;

/**
 * The Electrical Network in interface form.
 * 
 * @author Calclavia
 * 
 */
public interface IElectricityNetwork extends IGridNetwork<IElectricityNetwork, IConductor, TileEntity>
{
	/**
	 * Produces electricity in this electrical network.
	 * 
	 * @return Rejected energy in Joules.
	 */
	public float produce(ElectricityPack electricityPack, TileEntity... ignoreTiles);

	/**
	 * Gets the total amount of electricity requested/needed in the electricity network.
	 * 
	 * @param ignoreTiles The TileEntities to ignore during this calculation (optional).
	 */
	public ElectricityPack getRequest(TileEntity... ignoreTiles);

	/**
	 * @return The total amount of resistance of this electrical network. In Ohms.
	 */
	public float getTotalResistance();

	/**
	 * @return The lowest amount of current (amperage) that this electrical network can tolerate.
	 */
	public float getLowestCurrentCapacity();
}
