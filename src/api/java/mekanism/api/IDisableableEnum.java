package mekanism.api;

import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * Interface for enum's to make them easily incremental, while allowing for disabling various elements
 */
public interface IDisableableEnum<TYPE extends Enum<TYPE> & IDisableableEnum<TYPE>> extends IIncrementalEnum<TYPE> {

    /**
     * Used to check if a given element is enabled.
     *
     * @return {@code true} if the element is enabled, {@code false} otherwise.
     */
    boolean isEnabled();

    @Nonnull
    @Override
    default TYPE getNext(@Nonnull Predicate<TYPE> isValid) {
        return IIncrementalEnum.super.getNext(element -> element.isEnabled() && isValid.test(element));
    }

    @Nonnull
    @Override
    default TYPE getPrevious(@Nonnull Predicate<TYPE> isValid) {
        return IIncrementalEnum.super.getPrevious(element -> element.isEnabled() && isValid.test(element));
    }

    @Nonnull
    @Override
    default TYPE adjust(int shift) {
        //Note: We can just pass an always true predicate as we intercept getNext and getPrevious calls to
        // ensure that they test the element is enabled
        return adjust(shift, value -> true);
    }
}