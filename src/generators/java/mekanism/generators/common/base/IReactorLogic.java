package mekanism.generators.common.base;

import java.util.List;
import org.jetbrains.annotations.Unmodifiable;

public interface IReactorLogic<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> {

    TYPE getMode();

    @Unmodifiable
    List<TYPE> getModes();
}
