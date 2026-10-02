package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPersonalChest extends TileEntityPersonalStorage implements LidBlockEntity {

    private final ChestLidController chestLidController = new ChestLidController();

    public TileEntityPersonalChest(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PERSONAL_CHEST, pos, state);
    }

    @Override
    protected void onOpen(Level level, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(level, pos, state, SoundEvents.CHEST_OPEN);
    }

    @Override
    protected void onClose(Level level, BlockPos pos, BlockState state) {
        ChestBlockEntity.playSound(level, pos, state, SoundEvents.CHEST_CLOSE);
    }

    @Override
    protected Identifier getStat() {
        return Stats.OPEN_CHEST;
    }

    @Override
    protected void onUpdateClient(Level level) {
        super.onUpdateClient(level);
        chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == ChestBlock.EVENT_SET_OPEN_COUNT) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return chestLidController.getOpenness(partialTicks);
    }

    @Override
    public InteractionResult openGui(Level level, Player player) {
        BlockPos above = getBlockPos().above();
        if (level.getBlockState(above).isRedstoneConductor(level, above)) {
            //If the block above is solid consume the action
            return InteractionResult.CONSUME;
        }
        return super.openGui(level, player);
    }
}