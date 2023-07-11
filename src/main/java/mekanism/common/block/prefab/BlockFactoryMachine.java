package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockFactoryMachine<TILE extends TileEntityMekanism, MACHINE extends FactoryMachine<TILE>> extends BlockTile<TILE, MACHINE> {

    public BlockFactoryMachine(MACHINE machineType, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        super(machineType, propertiesModifier);
    }

    public static class BlockFactoryMachineModel<TILE extends TileEntityMekanism, MACHINE extends FactoryMachine<TILE>> extends BlockFactoryMachine<TILE, MACHINE> implements IStateFluidLoggable {

        public BlockFactoryMachineModel(MACHINE machineType, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
            super(machineType, propertiesModifier);
        }
    }

    public static class BlockFactory<TILE extends TileEntityFactory<?>> extends BlockFactoryMachineModel<TILE, Factory<TILE>> {

        public BlockFactory(Factory<TILE> factoryType) {
            super(factoryType, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor()));
        }
    }
}
