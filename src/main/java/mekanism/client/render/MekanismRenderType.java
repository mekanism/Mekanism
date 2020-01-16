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
        RenderType.State state = RenderType.State.func_228694_a_()
              .func_228724_a_(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .func_228723_a_(field_228520_l_)//shadeModel(GL11.GL_SMOOTH)
              .func_228713_a_(field_228516_h_)//disableAlphaTest
              .func_228726_a_(field_228515_g_)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .func_228728_a_(true);
        return func_228633_a_("mek_standard", DefaultVertexFormats.field_227849_i_, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType renderFlame(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State.func_228694_a_()
              .func_228724_a_(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .func_228723_a_(field_228520_l_)//shadeModel(GL11.GL_SMOOTH)
              .func_228713_a_(field_228516_h_)//disableAlphaTest
              .func_228726_a_(field_228515_g_)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .func_228728_a_(true);
        return func_228633_a_("mek_flame", DefaultVertexFormats.POSITION_TEX_COLOR, GL11.GL_QUADS, 256, true, false, state);
    }

    public static RenderType transmitterContents(ResourceLocation resourceLocation) {
        return func_228633_a_("transmitter_contents", DefaultVertexFormats.field_227849_i_, GL11.GL_QUADS, 256, true, false,
              renderFluidState(resourceLocation).func_228728_a_(true));
    }

    public static RenderType.State.Builder renderFluidState(ResourceLocation resourceLocation) {
        return RenderType.State.func_228694_a_()
              .func_228724_a_(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .func_228714_a_(field_228534_z_)//enableCull
              .func_228726_a_(field_228515_g_)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .func_228719_a_(field_228529_u_);//disableLighting
    }

    public static RenderType.State.Builder renderFluidTankState(ResourceLocation resourceLocation) {
        return renderFluidState(resourceLocation)
              .func_228723_a_(field_228520_l_)//shadeModel(GL11.GL_SMOOTH)
              .func_228713_a_(field_228516_h_);//disableAlphaTest
    }

    public static RenderType.State.Builder renderMechanicalPipeState(ResourceLocation resourceLocation) {
        return RenderType.State.func_228694_a_()
              .func_228724_a_(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .func_228714_a_(field_228534_z_)//enableCull
              .func_228719_a_(field_228529_u_);//disableLighting
    }

    public static RenderType.State.Builder configurableMachineState(ResourceLocation resourceLocation) {
        return RenderType.State.func_228694_a_()
              .func_228724_a_(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .func_228714_a_(field_228534_z_)//enableCull
              .func_228719_a_(field_228529_u_)//disableLighting
              .func_228723_a_(field_228520_l_)//shadeModel(GL11.GL_SMOOTH)
              .func_228713_a_(field_228516_h_)//disableAlphaTest
              .func_228726_a_(field_228515_g_);//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
    }

    public static RenderType resizableCuboid(RenderType.State.Builder stateBuilder, VertexFormat format) {
        stateBuilder.func_228713_a_(new RenderState.AlphaState(0.1F))//enableAlphaTest/alphaFunc(GL11.GL_GREATER, 0.1F)
              .func_228719_a_(field_228529_u_);//disableLighting
        return func_228633_a_("resizable_cuboid", format, GL11.GL_QUADS, 256, true, false, stateBuilder.func_228728_a_(true));
    }
}