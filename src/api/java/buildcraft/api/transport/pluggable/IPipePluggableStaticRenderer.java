package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableStaticRenderer {
    List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable, EnumFacing face);

    /** Use this if you need to render in the translucent pass. */
    public interface Translucent extends IPipePluggableStaticRenderer {
        /** For performance return a list that is as small as possible */
        List<BakedQuad> bakeTranslucent(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
                EnumFacing face);
    }
}
