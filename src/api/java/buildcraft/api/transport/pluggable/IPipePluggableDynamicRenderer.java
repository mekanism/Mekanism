package buildcraft.api.transport.pluggable;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipe;

@SideOnly(Side.CLIENT)
public interface IPipePluggableDynamicRenderer {
    void renderDynamicPluggable(IPipe pipe, EnumFacing side, PipePluggable pipePluggable, double x, double y, double z);
}
