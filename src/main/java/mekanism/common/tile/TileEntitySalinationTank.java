package mekanism.common.tile;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class TileEntitySalinationTank extends TileEntityContainerBlock
{
	TileEntitySalinationController master;

	public TileEntitySalinationTank()
	{
		super("SalinationTank");

		inventory = new ItemStack[0];
	}

	public TileEntitySalinationTank(String fullName)
	{
		super(fullName);

		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate() {};

	public void addToStructure(TileEntitySalinationController controller)
	{
		master = controller;
	}

	public void controllerGone()
	{
		master = null;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		if(master != null)
		{
			master.refresh(false);
		}
	}

	@Override
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);

		if(master != null)
		{
			master.refresh(false);
		}
	}
}
