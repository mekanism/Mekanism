package mekanism.common.capabilities;

import mekanism.api.energy.ICableOutputter;

import net.minecraft.util.EnumFacing;

/**
 * Created by ben on 30/04/16.
 */
public class CableOutputter implements ICableOutputter
{
    @Override
    public boolean canOutputTo(EnumFacing side)
    {
        return true;
    }
}
