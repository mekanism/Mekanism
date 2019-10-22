package mekanism.api;

import javax.annotation.Nonnull;

//TODO: Use both of these in various places and add JavaDocs
public interface IDisableableEnum<TYPE extends Enum<TYPE> & IDisableableEnum<TYPE>> extends IIncrementalEnum<TYPE> {

    boolean isEnabled();

    @Nonnull
    @Override
    default TYPE getNext() {
        return getNext(IDisableableEnum::isEnabled);
    }

    @Nonnull
    @Override
    default TYPE getPrevious() {
        return getPrevious(IDisableableEnum::isEnabled);
    }
}