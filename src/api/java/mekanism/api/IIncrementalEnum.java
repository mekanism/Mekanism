package mekanism.api;

import java.util.function.Predicate;
import javax.annotation.Nonnull;

//TODO: JavaDocs
public interface IIncrementalEnum<TYPE extends Enum<TYPE> & IIncrementalEnum<TYPE>> {

    @Nonnull
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

    @Nonnull
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

    //TODO: In java docs note while it can be static it then would lead to lots of duplicate code
    @Nonnull
    TYPE byIndex(int index);

    int ordinal();

    @Nonnull
    default TYPE getNext() {
        return getNext(type -> true);
    }

    @Nonnull
    default TYPE getPrevious() {
        return getPrevious(type -> true);
    }
}