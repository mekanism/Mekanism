package mekanism.common.tileentity;

import mekanism.common.block.BlockMachine.MachineType;

public class TileEntityDigitalMiner extends TileEntityElectricBlock
{
	public TileEntityDigitalMiner()
	{
		super("Digital Miner", MachineType.DIGITAL_MINER.baseEnergy);
	}
}
