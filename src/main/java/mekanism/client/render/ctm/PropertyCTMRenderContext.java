package mekanism.client.render.ctm;

import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * Block Property for the render context list
 */
public class PropertyCTMRenderContext implements IUnlistedProperty<CTMBlockRenderContext> 
{
    @Override
    public String getName()
    {
        return "connections";
    }

    @Override
    public boolean isValid(CTMBlockRenderContext value)
    {
        return true;
    }

    @Override
    public Class<CTMBlockRenderContext> getType()
    {
        return CTMBlockRenderContext.class;
    }

    @Override
    public String valueToString(CTMBlockRenderContext value)
    {
        return value.toString();
    }
}
