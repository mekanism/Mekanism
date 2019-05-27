package mekanism.common.recipe.inputs;

public interface IWildInput<INPUT extends MachineInput<INPUT>> {

    INPUT wildCopy();
}