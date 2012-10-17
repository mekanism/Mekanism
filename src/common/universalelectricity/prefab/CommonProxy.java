package universalelectricity.prefab;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * A class used to easily manuver between client and server.
 *
 */
public abstract class CommonProxy implements IGuiHandler
{
	public void preInit() { }
	
	public void init() { }
	
	public void postInit() { }
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }

}
