package buildcraft.api.blueprints;

import buildcraft.api.core.BuildCraftAPI;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class BlueprintManager {

	public static BptBlock[] blockBptProps = new BptBlock[Block.blocksList.length];

	public static ItemSignature getItemSignature(Item item) {
		ItemSignature sig = new ItemSignature();
	
		if (item.shiftedIndex >= Block.blocksList.length + BuildCraftAPI.LAST_ORIGINAL_ITEM) {
			sig.itemClassName = item.getClass().getSimpleName();
		}
	
		sig.itemName = item.getItemNameIS(new ItemStack(item));
	
		return sig;
	}

	public static BlockSignature getBlockSignature(Block block) {
		return BlueprintManager.blockBptProps[0].getSignature(block);
	}
	
	static {
		// Initialize defaults for block properties.
		for (int i = 0; i < BlueprintManager.blockBptProps.length; ++i) {
			new BptBlock(i);
		}
	}
}
