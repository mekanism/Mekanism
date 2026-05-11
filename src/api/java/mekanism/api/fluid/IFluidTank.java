package mekanism.api.fluid;

import com.mojang.serialization.Codec;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NothingNullByDefault
public interface IFluidTank extends IResourceContainer<FluidResource> {

    @Override
    @NonExtendable
    default Codec<LargeResourceStack<FluidResource>> resourceStackCodec() {
        return SerializerHelper.FLUID_RESOURCE_STACK_CODEC;
    }

    @Override
    @NonExtendable
    default LargeResourceStack<FluidResource> emptyStack() {
        return LargeResourceStack.EMPTY_FLUID_STACK;
    }
}