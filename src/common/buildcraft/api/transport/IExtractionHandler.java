package buildcraft.api.transport;

import net.minecraft.src.World;

/**
 * Implement and register with the PipeManager if you want to suppress connections from wooden pipes.
 */
public interface IExtractionHandler {

   /**
    * Can this pipe extract items from the block located at these coordinates?
    */
	boolean canExtractItems(IPipe pipe, World world, int i, int j, int k);
   
   /**
    * Can this pipe extract liquids from the block located at these coordinates?
    */
	boolean canExtractLiquids(IPipe pipe, World world, int i, int j, int k);
}
