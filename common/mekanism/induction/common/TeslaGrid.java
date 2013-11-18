/**
 * 
 */
package mekanism.induction.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mekanism.api.induction.ITesla;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TeslaGrid
{
	private static final TeslaGrid INSTANCE_CLIENT = new TeslaGrid();
	private static final TeslaGrid INSTANCE_SERVER = new TeslaGrid();

	private final Set<ITesla> tileEntities = new HashSet<ITesla>();

	public void register(ITesla iTesla)
	{
		Iterator<ITesla> it = tileEntities.iterator();

		while(it.hasNext())
		{
			ITesla tesla = it.next();

			if(tesla instanceof TileEntity)
			{
				if(!((TileEntity) tesla).isInvalid())
				{
					continue;
				}
			}

			it.remove();

		}

		tileEntities.add(iTesla);
	}

	public void unregister(ITesla iTesla)
	{
		tileEntities.remove(iTesla);
	}

	public Set<ITesla> get()
	{
		return tileEntities;
	}

	public static TeslaGrid instance()
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			return INSTANCE_SERVER;
		}

		return INSTANCE_CLIENT;
	}
}
