package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MekanismRenderType extends RenderType {

    private static final AlphaState CUBOID_ALPHA = new RenderState.AlphaState(0.1F);
    private static final RenderState.TransparencyState BLADE_TRANSPARENCY = new RenderState.TransparencyState("mek_blade_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, RenderSystem::disableBlend);
    private static final RenderState.TransparencyState PARTICLE_TRANSPARENCY = new RenderState.TransparencyState("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    public static final RenderType MEK_LIGHTNING = create("mek_lightning", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
          false, true, RenderType.State.builder()
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .setShadeModelState(SMOOTH_SHADE)
                .createCompositeState(false)
    );

    //Ignored
    private MekanismRenderType(String name, VertexFormat format, int drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, runnablePre, runnablePost);
    }

    public static RenderType mekStandard(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.builder()
              .setTextureState(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              .setAlphaState(NO_ALPHA)//disableAlphaTest
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .createCompositeState(true);
        return create("mek_standard", DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType bladeRender(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.builder()
              .setTextureState(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .setShadeModelState(SMOOTH_SHADE)
              .setTransparencyState(BLADE_TRANSPARENCY)
              .createCompositeState(true);
        return create("mek_blade", DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType renderFlame(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.builder()
              .setTextureState(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              .setAlphaState(NO_ALPHA)//disableAlphaTest
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .setLightmapState(NO_LIGHTMAP)//disableLighting
              .createCompositeState(true);
        return create("mek_flame", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType nutritionalParticle() {
        return create("mek_nutritional_particle", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256, false, false,
              RenderType.State.builder()
                    .setTextureState(new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(PARTICLE_TRANSPARENCY)
                    .setAlphaState(DEFAULT_ALPHA)
                    .createCompositeState(false)
        );
    }

    public static RenderType getMekaSuit() {
        RenderType.State state = RenderType.State.builder()
              .setTextureState(BLOCK_SHEET)
              .setDiffuseLightingState(DIFFUSE_LIGHTING)
              .setShadeModelState(SMOOTH_SHADE)
              .setAlphaState(MIDWAY_ALPHA)
              .setLightmapState(LIGHTMAP)
              .createCompositeState(true);
        return create("mekasuit", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 131_072, true, true, state);
    }

    public static RenderType renderSPS(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.builder()
              .setTextureState(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              .setTransparencyState(LIGHTNING_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .setLightmapState(NO_LIGHTMAP)
              .setAlphaState(CUBOID_ALPHA)
              .createCompositeState(true);
        return create("mek_sps", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, true, false, state);
    }
}