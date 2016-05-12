package mekanism.client.render.ctm;

import java.util.EnumMap;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class CTMBlockRenderContext 
{
    private EnumMap<EnumFacing, CTM> ctmData = new EnumMap<>(EnumFacing.class);

    public CTMBlockRenderContext(IBlockAccess world, BlockPos pos) 
    {
        for(EnumFacing face : EnumFacing.VALUES) 
        {
            CTM ctm = createCTM();
            ctm.createSubmapIndices(world, pos, face);
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
}
