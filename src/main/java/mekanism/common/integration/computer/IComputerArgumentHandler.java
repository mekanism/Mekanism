package mekanism.common.integration.computer;

import javax.annotation.Nullable;

public interface IComputerArgumentHandler<EXCEPTION extends Exception, RESULT> {

    int getCount();

    EXCEPTION error(String messageFormat, Object... args);

    @Nullable
    Object getArgument(int index);

    Object[] getArguments();

    RESULT noResult();

    RESULT wrapResult(Object result);
}