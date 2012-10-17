package ic2.api;

/**
 * Interface implemented by the tile entity of energy storage blocks.
 */
public interface IEnergyStorage {
	/**
	 * Get the amount of energy currently stored in the block.
	 * 
	 * @return Energy stored in the block
	 */
	public int getStored();
	
	/**
	 * Get the maximum amount of energy the block can store.
	 * 
	 * @return Maximum energy stored
	 */
	public int getCapacity();
	
	/**
	 * Get the block's energy output.
	 * 
	 * @return Energy output in EU/t
	 */
	public int getOutput();
}
