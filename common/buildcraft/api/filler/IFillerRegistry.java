package buildcraft.api.filler;

import net.minecraft.inventory.IInventory;

public interface IFillerRegistry {

	public void addRecipe(IFillerPattern pattern, Object aobj[]);

	public IFillerPattern findMatchingRecipe(IInventory inventorycrafting);

	public int getPatternNumber(IFillerPattern pattern);

	public IFillerPattern getPattern(int n);

}
