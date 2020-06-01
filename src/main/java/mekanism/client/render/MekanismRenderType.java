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
    private static final RenderState.TransparencyState BLADE_TRANSPARENCY = new RenderState.TransparencyState("blade_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, RenderSystem::disableBlend);

    //Ignored
    private MekanismRenderType(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
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

    public static RenderType transmitterContents(ResourceLocation resourceLocation) {
        return makeType("transmitter_contents", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false,
              preset(resourceLocation).build(true));
    }

    private static RenderType.State.Builder preset(ResourceLocation resourceLocation) {
        return RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .cull(CULL_ENABLED)//enableCull
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .shadeModel(SHADE_ENABLED);//shadeModel(GL11.GL_SMOOTH)
    }

    public static RenderType resizableCuboid() {
        RenderType.State.Builder stateBuilder = preset(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
              .alpha(CUBOID_ALPHA);//enableAlphaTest/alphaFunc(GL11.GL_GREATER, 0.1F)
        return makeType("resizable_cuboid", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256, true, false,
              stateBuilder.build(true));
    }

    public static RenderType renderSPS(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .lightmap(LIGHTMAP_DISABLED)
              .writeMask(WriteMaskState.COLOR_WRITE)
              .build(true);
        return makeType("mek_sps", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, true, true, state);
    }
}