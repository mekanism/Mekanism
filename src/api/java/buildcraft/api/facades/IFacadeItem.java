package buildcraft.api.facades;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public interface IFacadeItem {
	FacadeType getFacadeType(ItemStack facade);
	
	ItemStack getFacadeForBlock(Block block, int meta);
	
	Block[] getBlocksForFacade(ItemStack facade);

	int[] getMetaValuesForFacade(ItemStack facade);
}
