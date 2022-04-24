package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidProvider extends IBaseProvider {

    /**
     * Gets the fluid this provider represents.
     */
    @Nonnull
    Fluid getFluid();

    /**
     * Creates a fluid stack of the given size using the fluid this provider represents.
     *
     * @param size Size of the stack.
     */
    @Nonnull
    default FluidStack getFluidStack(int size) {
        return new FluidStack(getFluid(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return getFluid().getRegistryName();
    }

    @Override
    default Component getTextComponent() {
        return getFluid().getAttributes().getDisplayName(getFluidStack(1));
    }

    @Override
    default String getTranslationKey() {
        return getFluid().getAttributes().getTranslationKey();
    }
}