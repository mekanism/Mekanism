package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IGasProvider extends IBaseProvider {

    @Nonnull
    Gas getGas();

    @Nonnull
    default GasStack getGasStack(long size) {
        return new GasStack(getGas(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return getGas().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getGas().getTextComponent();
    }

    @Override
    default String getTranslationKey() {
        return getGas().getTranslationKey();
    }
}