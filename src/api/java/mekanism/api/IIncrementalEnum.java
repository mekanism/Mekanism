package mekanism.api;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;

/**
 * Interface for enum's to make them easily incremental
 */
@NothingNullByDefault
public interface IIncrementalEnum<TYPE extends Enum<TYPE> & IIncrementalEnum<TYPE>> {

    /**
     * Gets the next "valid" element
     *
     * @param isValid Predicate defining if an element is valid
     *
     * @return The next "valid" element
     */
    default TYPE getNext(Predicate<TYPE> isValid) {
        TYPE next = byIndex(ordinal() + 1);
        while (!isValid.test(next)) {
            if (next == this) {
                //Don't loop forever, and just return our self instead given we got back to our self
                return next;
            }
            next = byIndex(next.ordinal() + 1);
        }
        //Once we break out of the loop we know we have a valid entry
        return next;
    }

    /**
     * Gets the previous "valid" element
     *
     * @param isValid Predicate defining if an element is valid
     *
     * @return The previous "valid" element
     */
    default TYPE getPrevious(Predicate<TYPE> isValid) {
        TYPE previous = byIndex(ordinal() - 1);
        while (!isValid.test(previous)) {
            if (previous == this) {
                //Don't loop forever, and just return our self instead given we got back to our self
                return previous;
            }
            previous = byIndex(previous.ordinal() - 1);
        }
        //Once we break out of the loop we know we have a valid entry
        return previous;
    }

    /**
     * Helper method to get a value by index rather than having to duplicate all the previous/next logic.
     */
    TYPE byIndex(int index);

    /**
     * {@link Enum#ordinal()}
     */
    int ordinal();

    /**
     * Gets the next "valid" element
     *
     * @return The next "valid" element
     */
    default TYPE getNext() {
        return getNext(ConstantPredicates.alwaysTrue());
    }

    /**
     * Gets the previous "valid" element
     *
     * @return The previous "valid" element
     */
    default TYPE getPrevious() {
        return getPrevious(ConstantPredicates.alwaysTrue());
    }

    /**
     * Gets the "valid" element that is offset by the given shift
     *
     * @param shift Shift to perform, may be negative to indicate going backwards
     *
     * @return The "valid" element that is offset by the given shift
     *
     * @implNote Default implementation assumes all elements are "valid", override this if that is not the case.
     */
    default TYPE adjust(int shift) {
        return shift == 0 ? (TYPE) this : byIndex(ordinal() + shift);
    }

    /**
     * Gets the "valid" element that is offset by the given shift with the given validity predicate.
     *
     * @param shift   Shift to perform, may be negative to indicate going backwards
     * @param isValid Predicate defining if an element is valid
     *
     * @return The "valid" element that is offset by the given shift. If no elements are "valid" returns the current element.
     */
    default TYPE adjust(int shift, Predicate<TYPE> isValid) {
        TYPE result = (TYPE) this;
        while (shift < 0) {
            shift++;
            result = result.getPrevious(isValid);
        }
        while (shift > 0) {
            shift--;
            result = result.getNext(isValid);
        }
        return result;
    }
}