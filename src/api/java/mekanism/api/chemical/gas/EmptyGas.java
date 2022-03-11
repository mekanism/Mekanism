package mekanism.api.chemical.gas;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraftforge.registries.tags.IReverseTag;

public final class EmptyGas extends Gas {

    public EmptyGas() {
        super(GasBuilder.builder().hidden());
    }

    @Nonnull
    @Override
    protected Optional<IReverseTag<Gas>> getReverseTag() {
        //Empty gas is in no tags
        return Optional.empty();
    }
}