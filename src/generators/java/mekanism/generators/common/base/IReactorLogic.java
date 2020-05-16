package mekanism.generators.common.base;

public interface IReactorLogic<TYPE extends IReactorLogicMode> {

    TYPE getMode();

    TYPE[] getModes();
}
