package mekanism.common.tile.multiblock;

import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.MekanismMultiblocks;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager {

    public TileEntityDynamicTank(BlockPos pos, BlockState state) {
        this(MekanismBlocks.DYNAMIC_TANK, pos, state);
    }

    public TileEntityDynamicTank(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public InteractionResult onActivate(Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            TankMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                    if (ContainerType.FLUID.interactWithHandler(player, hand, null, multiblock.getDirectFluidHandler(), transaction) ||
                        ContainerType.CHEMICAL.interactWithHandler(player, hand, null, multiblock.getDirectChemicalHandler(), transaction)) {
                        transaction.commit();
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
                InteractionResult result = openGui(player);
                return result;
                //TODO - 26.1: why are these being remapped??
                /*return switch (result) {
                    case InteractionResult.SUCCESS, InteractionResult.SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                    case InteractionResult.CONSUME -> ItemInteractionResult.CONSUME;
                    case InteractionResult.CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                    case InteractionResult.PASS -> InteractionResult.TRY_WITH_EMPTY_HAND;
                    case InteractionResult.FAIL -> InteractionResult.FAIL;
                };*/
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @NotNull
    @Override
    public TankMultiblockData createMultiblock() {
        return new TankMultiblockData(this);
    }

    @Override
    public MultiblockType<TankMultiblockData> getMultiblockType() {
        return MekanismMultiblocks.TANK;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        return getMultiblock().editMode;
    }

    @Override
    public void nextMode() {
        TankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getNext());
    }

    @Override
    public void previousMode() {
        TankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getPrevious());
    }
}