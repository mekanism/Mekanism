package buildcraft.api.transport.pipe;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;

@SideOnly(Side.CLIENT)
public enum PipeApiClient {
    INSTANCE;

    public static IClientRegistry registry;

    public interface IClientRegistry {
        <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer);

        <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass, IPipeBehaviourRenderer<B> renderer);

        <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass, IPlugDynamicRenderer<P> renderer);

        <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass, IPluggableStaticBaker<P> renderer);
    }
}
