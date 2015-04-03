package mekanism.common.content.boiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.tile.TileEntityBoiler;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by ben on 09/01/15.
 */
public class BoilerManager extends MultiblockManager<SynchronizedBoilerData>
{
    public BoilerManager(String s)
    {
        super(s);
    }

    public void tickSelf(World world)
    {
        ArrayList<Integer> idsToKill = new ArrayList<Integer>();
        HashMap<Integer, HashSet<Coord4D>> tilesToKill = new HashMap<Integer, HashSet<Coord4D>>();

        for(Map.Entry<Integer, MultiblockCache<SynchronizedBoilerData>> entry : inventories.entrySet())
        {
            int inventoryID = entry.getKey();

            HashSet<TileEntityBoiler> boilers = new HashSet<TileEntityBoiler>();

            for(Coord4D obj : entry.getValue().locations)
            {
                if(obj.dimensionId == world.provider.dimensionId && obj.exists(world))
                {
                    TileEntity tileEntity = obj.getTileEntity(world);

                    if(!(tileEntity instanceof TileEntityMultiblock) || ((TileEntityMultiblock)tileEntity).getManager() != this || (getStructureId(((TileEntityMultiblock<?>)tileEntity)) != -1 && getStructureId(((TileEntityMultiblock)tileEntity)) != inventoryID))
                    {
                        if(!tilesToKill.containsKey(inventoryID))
                        {
                            tilesToKill.put(inventoryID, new HashSet<Coord4D>());
                        }

                        tilesToKill.get(inventoryID).add(obj);
                    }
                    else if(tileEntity instanceof TileEntityBoiler)
                    {
                        ((TileEntityBoiler)tileEntity).simulateHeat();
                        boilers.add((TileEntityBoiler) tileEntity);
                    }
                }
            }

            if(!boilers.isEmpty())
            {
                SynchronizedBoilerData data = boilers.iterator().next().getSynchronizedData();

                if(data != null)
                {
                    boilers.iterator().next().getSynchronizedData().applyTemperatureChange();
                }

                for (TileEntityBoiler boiler : boilers)
                {
                    boiler.applyTemperatureChange();
                }
            }

            if(entry.getValue().locations.isEmpty())
            {
                idsToKill.add(inventoryID);
            }
        }

        for(Map.Entry<Integer, HashSet<Coord4D>> entry : tilesToKill.entrySet())
        {
            for(Coord4D obj : entry.getValue())
            {
                inventories.get(entry.getKey()).locations.remove(obj);
            }
        }

        for(int inventoryID : idsToKill)
        {
            inventories.remove(inventoryID);
        }
    }
}
