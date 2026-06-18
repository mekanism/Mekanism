package mekanism.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_TRIANGLE_STRIP);
    }
}