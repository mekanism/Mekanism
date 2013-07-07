package buildcraft.api.transport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

public abstract class PipeManager {

	public static List<IExtractionHandler> extractionHandlers = new ArrayList<IExtractionHandler>();

	public static void registerExtractionHandler(IExtractionHandler handler) {
		extractionHandlers.add(handler);
	}

	/**
	 * param extractor can be null
	 */
	public static boolean canExtractItems(Object extractor, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers)
			if (!handler.canExtractItems(extractor, world, i, j, k))
				return false;

		return true;
	}
	
	/**
	 * param extractor can be null
	 */
	public static boolean canExtractLiquids(Object extractor, World world, int i, int j, int k) {
		for (IExtractionHandler handler : extractionHandlers)
			if (!handler.canExtractLiquids(extractor, world, i, j, k))
				return false;

		return true;
	}
}
