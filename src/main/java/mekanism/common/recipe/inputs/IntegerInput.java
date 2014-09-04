package mekanism.common.recipe.inputs;

public class IntegerInput extends MachineInput
{
	public int ingredient;

	public IntegerInput(int integer)
	{
		ingredient = integer;
	}


	@Override
	public int hashIngredients()
	{
		return ingredient;
	}

	@Override
	public boolean testEquality(MachineInput other)
	{
		return other instanceof IntegerInput && ingredient == ((IntegerInput)other).ingredient;
	}
}
