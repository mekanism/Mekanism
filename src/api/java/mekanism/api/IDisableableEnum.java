package mekanism.api;

import javax.annotation.Nonnull;

//TODO: Use both of these in various places and add JavaDocs
public interface IDisableableEnum<TYPE extends Enum<TYPE> & IDisableableEnum<TYPE>> extends IIncrementalEnum<TYPE> {

    boolean isEnabled();

    @Nonnull
    @Override
    default TYPE getNext() {
        //Note: Do not replace this with method reference, or it will crash not being able to resolve the TYPE
        return getNext(element -> element.isEnabled());
    }

    @Nonnull
    @Override
    default TYPE getPrevious() {
        //Note: Do not replace this with method reference, or it will crash not being able to resolve the TYPE
        return getPrevious(element -> element.isEnabled());
    }
}