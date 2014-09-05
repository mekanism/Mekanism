package mekanism.common.recipe.outputs;

public abstract class MachineOutput<OUTPUT extends MachineOutput<OUTPUT>>
{
	public abstract OUTPUT copy();
}
