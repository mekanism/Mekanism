package mekanism.induction.common.wire;

import net.minecraftforge.common.ForgeDirection;

public interface IBlockableConnection
{
	public boolean isBlockedOnSide(ForgeDirection side);
}
