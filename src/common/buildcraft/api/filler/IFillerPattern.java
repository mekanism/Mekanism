package buildcraft.api.filler;

import buildcraft.api.core.IBox;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public interface IFillerPattern {

	public int getId();

	public void setId(int id);

	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace);

	public String getTextureFile();

	public int getTextureIndex();

	public String getName();

}
