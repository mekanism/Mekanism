package obsidian.api;

import net.minecraftforge.common.ForgeDirection;

public interface IEnergyAcceptor 
{
	public int transferToAcceptor(int amount);
	
	public boolean canReceive(ForgeDirection side);
}
