package mekanism.api.math;

import java.util.function.Supplier;
import javax.annotation.Nonnull;

//TODO: Docs? This class is mainly to declare get as nonnull and also make it easier to reference the supplier
@FunctionalInterface
public interface FloatingLongSupplier extends Supplier<FloatingLong> {

    @Nonnull
    @Override
    FloatingLong get();

    //TODO: Do we want to add helper getters, that say get and multiply at the same time?
}