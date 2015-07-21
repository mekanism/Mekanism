package buildcraft.api.transport.pluggable;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableDynamicRenderer {
	void renderPluggable(IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, double x, double y, double z);
}
