package mekanism.api.math;

import java.util.function.Consumer;
import javax.annotation.Nonnull;

//TODO: Docs? This class is mainly to declare get as nonnull and also make it easier to reference the consumer
@FunctionalInterface
public interface FloatingLongConsumer extends Consumer<FloatingLong> {

    @Override
    void accept(@Nonnull FloatingLong floatingLong);
}