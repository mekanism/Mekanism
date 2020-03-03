package mekanism.common.block.machine.prefab;

import javax.annotation.Nonnull;
import mekanism.api.block.FactoryType;
import mekanism.api.block.IHasFactoryType;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.content.machines.Machine.FactoryMachine;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;

public class BlockFactoryMachine<TILE extends TileEntityMekanism> extends BlockMachine<TILE, FactoryMachine<TILE>> implements IHasFactoryType, IUpgradeableBlock {

    public BlockFactoryMachine(FactoryMachine<TILE> machineType) {
        super(machineType);
    }

    @Nonnull
    @Override
    public FactoryType getFactoryType() {
        return machineType.getFactoryType();
    }

    @Nonnull
    @Override
    public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return machineType.upgradeResult(current, tier);
    }

    public static class BlockFactoryMachineModel<TILE extends TileEntityMekanism> extends BlockMachineModel<TILE, FactoryMachine<TILE>> {

        public BlockFactoryMachineModel(FactoryMachine<TILE> machineType) {
            super(machineType);
        }
    }
}
