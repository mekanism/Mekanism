package mekanism.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class MekanismRenderPipelines {

    private static final BlendFunction DST_FUNCTION = new BlendFunction(BlendFactor.DST_COLOR, BlendFactor.ZERO);

    public static final RenderPipeline GUI_DST_COLOR = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/gui_dst_color"))
          .withColorTargetState(new ColorTargetState(DST_FUNCTION))
          .build();

    public static final RenderPipeline GUI_TEXTURED_DST_COLOR = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/gui_textured_dst_color"))
          .withColorTargetState(new ColorTargetState(DST_FUNCTION))
          .build();

    /// Like [RenderPipelines#GUI] but with TriangleStrip topology
    public static final RenderPipeline GUI_TRIANGLE_STRIP = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/gui_triangle_strip"))
          .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
          .build();

    public static final RenderPipeline MEKASUIT = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/mekasuit"))
          .withVertexShader(Mekanism.rl("core/mekasuit"))
          .withFragmentShader(Mekanism.rl("core/mekasuit"))
          .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
          .withVertexBinding(0, DefaultVertexFormat.ENTITY)
          .withPrimitiveTopology(PrimitiveTopology.QUADS)
          .withDepthStencilState(DepthStencilState.DEFAULT)
          //The above is from ENTITY_SNIPPET, the below is from ENTITY_CUTOUT
          //Note: Don't limit the alpha of the passed tint, we skip using this if the tint is fully transparent anyway
          //.withShaderDefine("ALPHA_CUTOUT", 0.1F)
          .withShaderDefine("PER_FACE_LIGHTING")
          .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
          .withCull(false)
          .build();

    //Pipeline is from lightning
    public static final RenderPipeline SPS = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/sps"))
          .withVertexShader(Mekanism.rl("core/sps"))
          .withFragmentShader(Mekanism.rl("core/sps"))
          .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
          .withPrimitiveTopology(PrimitiveTopology.QUADS)
          //From lightning
          .withDepthStencilState(DepthStencilState.DEFAULT)
          .build();

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_DST_COLOR);
        event.registerPipeline(GUI_TEXTURED_DST_COLOR);
        event.registerPipeline(GUI_TRIANGLE_STRIP);
        event.registerPipeline(MEKASUIT);
        event.registerPipeline(SPS);
    }
}