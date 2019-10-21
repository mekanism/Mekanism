package mekanism.api;

import java.util.function.Predicate;
import javax.annotation.Nonnull;

//TODO: Make two interfaces that enums can implement.
// 1. IDisableableEnum: Lets values be marked as "disabled" so can be skipped when calculating which element we are on (see atomic disassembler modes)
// 2. IIncrementalEnum: With some methods to "help" increment/decrement which index we are at
//TODO: Use this in places, and add support for things if they implement BOTH interfaces
//TODO: JavaDocs
public interface IIncrementalEnum<TYPE extends Enum<TYPE>> {

    @Nonnull
    TYPE getNext(Predicate<TYPE> isValid);

    @Nonnull
    TYPE getPrevious(Predicate<TYPE> isValid);

    @Nonnull
    default TYPE getNext() {
        return getNext(type -> {
            if (type instanceof IDisableableEnum) {
                return ((IDisableableEnum<?>) type).isEnabled();
            }
            return true;
        });
    }

    @Nonnull
    default TYPE getPrevious() {
        return getPrevious(type -> {
            if (type instanceof IDisableableEnum) {
                return ((IDisableableEnum<?>) type).isEnabled();
            }
            return true;
        });
    }
}