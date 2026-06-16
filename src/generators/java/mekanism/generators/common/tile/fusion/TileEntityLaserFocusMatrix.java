package mekanism.generators.common.tile.fusion;

import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TileEntityLaserFocusMatrix extends TileEntityFusionReactorBlock implements ILaserReceptor {

    public TileEntityLaserFocusMatrix(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.LASER_FOCUS_MATRIX, pos, state);
    }

    @Override
    public int receiveLaserEnergy(int energy, TransactionContext transaction) {
        FusionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            multiblock.addTemperatureFromEnergyInput(energy, transaction);
            return energy;
        }
        return 0;
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            FusionReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                    multiblock.setPlasmaTemp(10 * FusionReactorMultiblockData.BURN_TEMPERATURE, transaction);
                    transaction.commit();
                }
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        //TODO - 26.2: Don't call super on the client side if it was successful?
        return super.onRightClick(level, player);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}