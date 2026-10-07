package mekanism.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
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

    private static final RenderPipeline.Snippet MEKASUIT_SNIPPET = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
          .withVertexShader(Mekanism.rl("core/mekasuit"))
          .withFragmentShader(Mekanism.rl("core/mekasuit"))
          .buildSnippet();

    /// Based on [RenderPipelines#ARMOR_CUTOUT_NO_CULL]
    public static final RenderPipeline MEKASUIT = RenderPipeline.builder(MEKASUIT_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/mekasuit"))
          .withShaderDefine("ALPHA_CUTOUT", 0.1F)
          .withShaderDefine("NO_OVERLAY")
          .withShaderDefine("PER_FACE_LIGHTING")
          .withCull(false)
          .withColorTargetState(ColorTargetState.DEFAULT)
          .build();
    /// Based on [RenderPipelines#ARMOR_CUTOUT_NO_CULL_GLINT]
    public static final RenderPipeline MEKASUIT_GLINT = RenderPipeline.builder(MEKASUIT_SNIPPET, RenderPipelines.GLINT_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/mekasuit_glint"))
          .withShaderDefine("ALPHA_CUTOUT", 0.1F)
          .withShaderDefine("NO_OVERLAY")
          .withShaderDefine("PER_FACE_LIGHTING")
          .withCull(false)
          .withColorTargetState(ColorTargetState.DEFAULT)
          .build();
    //TODO: Is there a vanilla OitPipelineSet we can use in place of this?
    public static final OitPipelineSet OIT_ARMOR_TRANSLUCENT_GLINT = OitPipelineSet.builder(Mekanism.rl("pipeline/armor_translucent_glint"), RenderPipeline.builder(RenderPipelines.OIT_ENTITY_SNIPPET))
          .withAccumulateModifier(accumulate -> accumulate
                .withShaderDefine("PER_FACE_LIGHTING")
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
          )
          .build();

    //Pipeline is from lightning
    private static final RenderPipeline.Snippet SPS_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withVertexShader(Mekanism.rl("core/sps"))
          .withFragmentShader(Mekanism.rl("core/sps"))
          .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
          .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
          .withPrimitiveTopology(PrimitiveTopology.QUADS)
          //From lightning
          .withDepthStencilState(DepthStencilState.DEFAULT)
          .buildSnippet();

    public static final RenderPipeline SPS = RenderPipeline.builder(SPS_SNIPPET)
          .withLocation(Mekanism.rl("pipeline/sps"))
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .build();

    public static final OitPipelineSet OIT_SPS = OitPipelineSet.builder(Mekanism.rl("pipeline/sps"), RenderPipeline.builder(SPS_SNIPPET)
                .withShaderDefine("OIT_ADDITIVE")
          ).withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER0))
          .build();


    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_DST_COLOR);
        event.registerPipeline(GUI_TEXTURED_DST_COLOR);
        event.registerPipeline(GUI_TRIANGLE_STRIP);
        event.registerPipeline(MEKASUIT);
        event.registerPipeline(MEKASUIT_GLINT);

        event.registerOitPipelineSet(OIT_ARMOR_TRANSLUCENT_GLINT);

        event.registerPipeline(SPS);
        event.registerOitPipelineSet(OIT_SPS);
    }
}