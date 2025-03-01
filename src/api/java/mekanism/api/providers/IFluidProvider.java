package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

@MethodsReturnNonnullByDefault
public interface IFluidProvider extends IBaseProvider {

    /**
     * Gets the fluid this provider represents.
     */
    Fluid getFluid();

    /**
     * Helper method to get the holder that corresponds to this provider.
     *
     * @since 10.7.11
     */
    @SuppressWarnings("deprecation")
    default Holder<Fluid> getFluidHolder() {
        return getFluid().builtInRegistryHolder();
    }

    /**
     * Creates a fluid stack of the given size using the fluid this provider represents.
     *
     * @param size Size of the stack.
     */
    default FluidStack getFluidStack(int size) {
        return new FluidStack(getFluidHolder(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return BuiltInRegistries.FLUID.getKey(getFluid());
    }

    @Override
    default Component getTextComponent() {
        return getFluid().getFluidType().getDescription(getFluidStack(1));
    }

    @Override
    default String getTranslationKey() {
        return getFluid().getFluidType().getDescriptionId();
    }
}