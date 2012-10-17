package universalelectricity.prefab.chunk;

import net.minecraft.src.Chunk;

/**
 * Applied to all objects that requires to be called when a chunk loads. When applied to tile entities, it will 
 * @author Calclavia
 *
 */
public interface IChunkLoadHandler
{
	/**
	 * Called when a chunk loads.
	 */
	public void onChunkLoad(Chunk chunk);

	/**
	 * Called when a chunk loads.
	 */
	public void onChunkUnload(Chunk chunk);
}
