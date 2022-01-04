package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class MekanismRenderType extends RenderType {

    //Ignored
    private MekanismRenderType(String name, VertexFormat format, Mode drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, runnablePre, runnablePost);
    }

    //TODO - 1.18: Figure out
    //private static final AlphaStateShard CUBOID_ALPHA = new RenderStateShard.AlphaStateShard(0.1F);
    private static final RenderStateShard.TransparencyStateShard BLADE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_blade_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, RenderSystem::disableBlend);
    private static final RenderStateShard.TransparencyStateShard PARTICLE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    public static final RenderType MEK_LIGHTNING = create("mek_lightning", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256,
          false, true, RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                //TODO - 1.18: Figure out
                //.setShadeModelState(SMOOTH_SHADE)
                .createCompositeState(false)
    );

    public static RenderType standard(ResourceLocation resourceLocation) {
        return STANDARD.apply(resourceLocation);
    }

    public static final Function<ResourceLocation, RenderType> STANDARD = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.NEW_ENTITY_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))//Texture state
              //TODO - 1.18: Figure out
              //.setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              //.setAlphaState(NO_ALPHA)//disableAlphaTest
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .createCompositeState(true);
        return create("mek_standard", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
    });

    public static final Function<ResourceLocation, RenderType> BLADE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.NEW_ENTITY_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))//Texture state
              //TODO - 1.18: Figure out
              //.setShadeModelState(SMOOTH_SHADE)
              .setTransparencyState(BLADE_TRANSPARENCY)
              .createCompositeState(true);
        return create("mek_blade", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
    });

    public static final Function<ResourceLocation, RenderType> FLAME = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))//Texture state
              //TODO - 1.18: Figure out
              //.setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              //.setAlphaState(NO_ALPHA)//disableAlphaTest
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .createCompositeState(true);
        return create("mek_flame", DefaultVertexFormat.POSITION_COLOR_TEX, Mode.QUADS, 256, true, false, state);
    });

    public static final RenderType NUTRITIONAL_PARTICLE = create("mek_nutritional_particle", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, Mode.QUADS,
          256, false, false, RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                .setTransparencyState(PARTICLE_TRANSPARENCY)
                //TODO - 1.18: Figure out
                //.setAlphaState(DEFAULT_ALPHA)
                //TODO - 1.18: Are we meant to be calling .setLightmapState(LIGHTMAP) given default is no lightmap?
                .createCompositeState(false)
    );

    public static final RenderType MEKASUIT = create("mekasuit", DefaultVertexFormat.BLOCK, Mode.QUADS, 131_072, true, true,
          RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.BLOCK_SHADER)
                .setTextureState(BLOCK_SHEET)
                //TODO - 1.18: Figure out
                //.setDiffuseLightingState(DIFFUSE_LIGHTING)
                //.setShadeModelState(SMOOTH_SHADE)
                //.setAlphaState(MIDWAY_ALPHA)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(true)
    );

    public static final Function<ResourceLocation, RenderType> SPS = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))//Texture state
              //TODO - 1.18: Figure out
              //.setShadeModelState(SMOOTH_SHADE)//shadeModel(GL11.GL_SMOOTH)
              .setTransparencyState(LIGHTNING_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .setLightmapState(NO_LIGHTMAP)
              //.setAlphaState(CUBOID_ALPHA)
              .createCompositeState(true);
        return create("mek_sps", DefaultVertexFormat.POSITION_COLOR_TEX, Mode.QUADS, 256, true, false, state);
    });
}