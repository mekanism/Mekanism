package mekanism.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismShaders {

    static final ShaderTracker MEKASUIT = new ShaderTracker();
    //Merge of position_color_tex and rendertype_lightning
    static final ShaderTracker SPS = new ShaderTracker();

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        registerShader(event, Mekanism.rl("rendertype_mekasuit"), DefaultVertexFormat.NEW_ENTITY, MEKASUIT);
        registerShader(event, Mekanism.rl("rendertype_sps"), DefaultVertexFormat.POSITION_COLOR_TEX, SPS);
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), shaderLocation, vertexFormat), tracker::setInstance);
    }

    static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final ShaderStateShard shard = new ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}