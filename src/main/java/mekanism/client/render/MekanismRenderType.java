package mekanism.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MekanismRenderType extends RenderType {

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

    public static RenderType renderFlame(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .alpha(ZERO_ALPHA)//disableAlphaTest
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .build(true);
        return makeType("mek_flame", DefaultVertexFormats.POSITION_TEX_COLOR, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType transmitterContents(ResourceLocation resourceLocation) {
        return makeType("transmitter_contents", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false,
              renderFluidState(resourceLocation).build(true));
    }

    public static RenderType.State.Builder renderFluidState(ResourceLocation resourceLocation) {
        return RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .cull(CULL_ENABLED)//enableCull
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .lightmap(LIGHTMAP_DISABLED);//disableLighting
    }

    public static RenderType.State.Builder renderFluidTankState(ResourceLocation resourceLocation) {
        return renderFluidState(resourceLocation)
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .alpha(ZERO_ALPHA);//disableAlphaTest
    }

    public static RenderType.State.Builder renderMechanicalPipeState(ResourceLocation resourceLocation) {
        return RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .cull(CULL_ENABLED)//enableCull
              .lightmap(LIGHTMAP_DISABLED);//disableLighting
    }

    public static RenderType.State.Builder configurableMachineState(ResourceLocation resourceLocation) {
        return RenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .cull(CULL_ENABLED)//enableCull
              .lightmap(LIGHTMAP_DISABLED)//disableLighting
              .shadeModel(SHADE_ENABLED)//shadeModel(GL11.GL_SMOOTH)
              .alpha(ZERO_ALPHA)//disableAlphaTest
              .transparency(TRANSLUCENT_TRANSPARENCY);//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
    }

    public static RenderType resizableCuboid(RenderType.State.Builder stateBuilder, VertexFormat format) {
        stateBuilder.alpha(new RenderState.AlphaState(0.1F))//enableAlphaTest/alphaFunc(GL11.GL_GREATER, 0.1F)
              .lightmap(LIGHTMAP_DISABLED);//disableLighting
        return makeType("resizable_cuboid", format, GL11.GL_QUADS, 256, true, false, stateBuilder.build(true));
    }
}