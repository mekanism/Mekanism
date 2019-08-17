package mekanism.common.base;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class LazyOptionalHelper<T> {

    private LazyOptional<T> lazyOptional;

    public LazyOptionalHelper(@Nonnull LazyOptional<T> lazyOptional) {
        this.lazyOptional = lazyOptional;
    }

    public boolean isPresent() {
        return lazyOptional.isPresent();
    }

    /**
     * Used to not have null warnings everywhere, {@link LazyOptional#orElse(Object)} does not error when passed a null value though it is annotated Nonnull
     *
     * @return The value of the optional or null if there is none.
     */
    @Nullable
    public T getValue() {
        return lazyOptional.orElse(null);
    }

    public void ifPresent(@Nonnull NonNullConsumer<? super T> consumer) {
        lazyOptional.ifPresent(consumer);
    }

    public void ifPresentElse(NonNullConsumer<? super T> presentConsumer, Runnable elseConsumer) {
        if (isPresent()) {
            lazyOptional.ifPresent(presentConsumer);
        } else {
            elseConsumer.run();
        }
    }

    @Nullable
    public <RESULT> RESULT getIfPresent(Function<? super T, RESULT> function) {
        if (isPresent()) {
            return function.apply(getValue());
        }
        return null;
    }

    //For when the result is not a constant so we don't want to evaluate it if our lazyoptional is present
    public <RESULT> RESULT getIfPresentElseDo(Function<? super T, RESULT> presentFunction, Supplier<RESULT> elseResult) {
        if (isPresent()) {
            return presentFunction.apply(getValue());
        }
        return elseResult.get();
    }

    public <RESULT> RESULT getIfPresentElse(Function<? super T, RESULT> presentFunction, RESULT elseResult) {
        if (isPresent()) {
            return presentFunction.apply(getValue());
        }
        return elseResult;
    }

    public <TYPE extends T> boolean matches(Predicate<TYPE> predicate) {
        if (isPresent()) {
            return predicate.test((TYPE) getValue());
        }
        return false;
    }
}