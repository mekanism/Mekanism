package mekanism.client.render.ctm;

import java.util.EnumMap;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.ArrayUtils;

import static mekanism.client.render.ctm.CTMConnections.*;

public class CTMBlockRenderContext 
{
	private static final CTMConnections[] CACHED_LOCATIONS = ArrayUtils.removeElements(CTMConnections.VALUES, UP_UP, DOWN_DOWN, EAST_EAST, WEST_WEST, NORTH_NORTH, SOUTH_SOUTH);
	
    private EnumMap<EnumFacing, CTM> ctmData = new EnumMap<>(EnumFacing.class);
    
    private long data;

    public CTMBlockRenderContext(IBlockAccess world, BlockPos pos) 
    {
        for(EnumFacing face : EnumFacing.VALUES) 
        {
            CTM ctm = createCTM();
            ctm.createSubmapIndices(world, pos, face);
            ctmData.put(face, ctm);
        }
        
        data = CTMConnections.getData(world, pos, CACHED_LOCATIONS);
    }
    
    public CTMBlockRenderContext(long d)
    {
        data = d;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            CTM ctm = createCTM();
            ctm.createSubmapIndices(data, face);
            ctmData.put(face, ctm);
        }
    }
    
    protected CTM createCTM() 
    {
        return CTM.getInstance();
    }

    public CTM getCTM(EnumFacing face) 
    {
        return ctmData.get(face);
    }
    
    public long serialize()
    {
    	return data;
    }
}
