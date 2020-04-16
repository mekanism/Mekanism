package mekanism.api;

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
    @SuppressWarnings("Convert2MethodRef")
    default TYPE getNext() {
        //Note: Do not replace this with method reference, or it will crash not being able to resolve the TYPE
        return getNext(element -> element.isEnabled());
    }

    @Nonnull
    @Override
    @SuppressWarnings("Convert2MethodRef")
    default TYPE getPrevious() {
        //Note: Do not replace this with method reference, or it will crash not being able to resolve the TYPE
        return getPrevious(element -> element.isEnabled());
    }

    @Nonnull
    @Override
    default TYPE adjust(int shift) {
        TYPE result = (TYPE) this;
        while (shift < 0) {
            shift++;
            result = result.getPrevious();
        }
        while (shift > 0) {
            shift--;
            result = result.getNext();
        }
        return result;
    }
}