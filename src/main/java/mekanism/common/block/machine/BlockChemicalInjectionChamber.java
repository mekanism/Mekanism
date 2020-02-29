package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;

public class BlockChemicalInjectionChamber extends BlockFactoryMachine<TileEntityChemicalInjectionChamber> {

    public BlockChemicalInjectionChamber() {
        super(MekanismMachines.CHEMICAL_INJECTION_CHAMBER);
    }
}