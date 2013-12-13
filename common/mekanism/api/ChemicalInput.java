package mekanism.api;

import mekanism.api.gas.Gas;

public class ChemicalInput
{
	public Gas leftGas;
	public Gas rightGas;
	
	public ChemicalInput(Gas left, Gas right)
	{
		leftGas = left;
		rightGas = right;
	}
	
	public boolean isValid()
	{
		return leftGas != null && rightGas != null;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof ChemicalInput))
		{
			return false;
		}
		
		ChemicalInput compare = (ChemicalInput)obj;
		
		if(leftGas == compare.leftGas && rightGas == compare.rightGas)
		{
			return true;
		}
		else if(leftGas == compare.rightGas && rightGas == compare.leftGas)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + leftGas.getID();
		code = 31 * code + rightGas.getID();
		return code;
	}
}
