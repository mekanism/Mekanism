package mekanism.common.block.machine.prefab;

import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;

public class BlockFactoryMachine<TILE extends TileEntityMekanism, MACHINE extends FactoryMachine<TILE>> extends BlockTile<TILE, MACHINE> {

    public BlockFactoryMachine(MACHINE machineType) {
        super(machineType);
    }

    public static class BlockFactoryMachineModel<TILE extends TileEntityMekanism> extends BlockFactoryMachine<TILE, FactoryMachine<TILE>> implements IStateFluidLoggable {

        public BlockFactoryMachineModel(FactoryMachine<TILE> machineType) {
            super(machineType);
        }
    }

    public static class BlockFactory<TILE extends TileEntityFactory<?>> extends BlockFactoryMachine<TILE, Factory<TILE>> implements ITieredBlock<FactoryTier> {

        public BlockFactory(Factory<TILE> factoryType) {
            super(factoryType);
        }

        @Override
        public FactoryTier getTier() {
            return type.getTier();
        }
    }
}
