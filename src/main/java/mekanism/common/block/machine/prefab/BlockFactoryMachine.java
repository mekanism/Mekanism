package mekanism.common.block.machine.prefab;

import javax.annotation.Nonnull;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.block.BlockState;

public class BlockFactoryMachine<TILE extends TileEntityMekanism, MACHINE extends FactoryMachine<TILE>> extends BlockMachine<TILE, MACHINE> implements IUpgradeableBlock {

    public BlockFactoryMachine(MACHINE machineType) {
        super(machineType);
    }

    @Nonnull
    @Override
    public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return machineType.upgradeResult(current, tier);
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
            return machineType.getTier();
        }
    }
}
