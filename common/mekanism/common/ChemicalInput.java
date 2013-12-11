package mekanism.common;

import mekanism.api.Object3D;
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
		return obj instanceof ChemicalInput &&
				((ChemicalInput)obj).leftGas == leftGas &&
				((ChemicalInput)obj).rightGas == rightGas;
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
