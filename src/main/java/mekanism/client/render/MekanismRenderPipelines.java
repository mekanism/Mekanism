package mekanism.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class MekanismRenderPipelines {

    ///Like [RenderPipelines#GUI] but with TriangleStrip topology
    public static final RenderPipeline GUI_TRIANGLE_STRIP = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/gui_triangle_strip"))
          .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
          .build();

    public static final RenderPipeline WARNING_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/gui_textured_dst_color"))
          .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.DST_COLOR, BlendFactor.ZERO)))
          .build();

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_TRIANGLE_STRIP);
        event.registerPipeline(WARNING_PIPELINE);
    }
}