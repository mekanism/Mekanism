package buildcraft.api.transport.pluggable;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;

public interface IPipePluggableRenderer {
	void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side,
						 PipePluggable pipePluggable, ITextureStates blockStateMachine,
						 int renderPass, int x, int y, int z);
}
