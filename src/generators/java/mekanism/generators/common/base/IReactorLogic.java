package mekanism.generators.common.base;

public interface IReactorLogic<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> {

    TYPE getMode();

    TYPE[] getModes();
}
