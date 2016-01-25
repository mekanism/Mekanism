package buildcraft.api.transport.pluggable;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableDynamicRenderer {
    void renderDynamicPluggable(IPipe pipe, EnumFacing side, PipePluggable pipePluggable, double x, double y, double z);
}
