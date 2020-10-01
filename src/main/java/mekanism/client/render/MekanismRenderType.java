package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MekanismRenderType extends RenderType {

    private static final AlphaState CUBOID_ALPHA = new RenderState.AlphaState(0.1F);
    private static final RenderState.TransparencyState BLADE_TRANSPARENCY = new RenderState.TransparencyState("blade_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, RenderSystem::disableBlend);

    public static final RenderType MEK_LIGHTNING = makeType("mek_lightning", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
          false, true, RenderType.State.getBuilder()
                .writeMask(COLOR_DEPTH_WRITE)
                .transparency(LIGHTNING_TRANSPARENCY)
                .shadeModel(SHADE_ENABLED)
                .build(false)
    );

    //Ignored
    private MekanismRenderType(String name, VertexFormat format, int drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, runnablePre, runnablePost);
    }

    public static RenderType mekStandard(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .alpha(ZERO_ALPHA)//disableAlphaTest
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .build(true);
        return makeType("mek_standard", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType bladeRender(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)
              .transparency(BLADE_TRANSPARENCY)
              .build(true);
        return makeType("mek_blade", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType renderFlame(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .alpha(ZERO_ALPHA)//disableAlphaTest
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .lightmap(LIGHTMAP_DISABLED)//disableLighting
              .build(true);
        return makeType("mek_flame", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType getEntityTranslucentCullNoDiffuse(ResourceLocation locationIn) {
        //Copy of getEntityTranslucentCull except without enabling diffuse lighting
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(locationIn, false, false))
              .transparency(TRANSLUCENT_TRANSPARENCY)
              .alpha(DEFAULT_ALPHA)
              .lightmap(LIGHTMAP_ENABLED)
              .overlay(OVERLAY_ENABLED)
              .build(true);
        return makeType("entity_translucent_cull_no_diffuse", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, true, state);
    }

    public static RenderType getMekaSuit() {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(BLOCK_SHEET)
              .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
              .shadeModel(SHADE_ENABLED)
              .alpha(HALF_ALPHA)
              .lightmap(LIGHTMAP_ENABLED)
              .build(true);
        return makeType("mekasuit", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 131_072, true, true, state);
    }

    public static RenderType renderSPS(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .transparency(LIGHTNING_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .lightmap(LIGHTMAP_DISABLED)
              .alpha(CUBOID_ALPHA)
              .build(true);
        return makeType("mek_sps", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, true, false, state);
    }
}