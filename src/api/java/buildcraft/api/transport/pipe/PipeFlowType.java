package buildcraft.api.transport.pipe;

import net.minecraft.nbt.NBTTagCompound;

public final class PipeFlowType {
    public final IFlowCreator creator;
    public final IFlowLoader loader;

    public PipeFlowType(IFlowCreator creator, IFlowLoader loader) {
        this.creator = creator;
        this.loader = loader;
    }

    @FunctionalInterface
    public interface IFlowCreator {
        PipeFlow createFlow(IPipe t);
    }

    @FunctionalInterface
    public interface IFlowLoader {
        PipeFlow loadFlow(IPipe t, NBTTagCompound u);
    }
}
