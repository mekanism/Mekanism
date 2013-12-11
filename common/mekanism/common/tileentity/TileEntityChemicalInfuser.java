package mekanism.common.tileentity;

import mekanism.api.gas.GasStack;

public class TileEntityChemicalInfuser extends TileEntityElectricBlock
{
	public GasStack leftStack;
	public GasStack rightStack;
	
	public TileEntityChemicalInfuser()
	{
		super("ChemicalInfuser", 0 /*TODO*/);
	}
}
