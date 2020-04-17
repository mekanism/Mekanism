package mekanism.generators.common.base;

public interface IReactorLogic<TYPE extends IReactorLogicMode> {

    public TYPE getMode();

    public TYPE[] getModes();
}
