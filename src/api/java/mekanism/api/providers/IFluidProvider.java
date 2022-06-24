package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

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
        return ForgeRegistries.FLUIDS.getKey(getFluid());
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