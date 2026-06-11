package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIndustrialAlarm extends TileEntityMekanism {

    public TileEntityIndustrialAlarm(BlockPos pos, BlockState state) {
        super(MekanismBlocks.INDUSTRIAL_ALARM, pos, state);
        delaySupplier = () -> 3;
    }

    @Override
    public void onPowerChange(LevelReader level) {
        super.onPowerChange(level);
        if (!level.isClientSide()) {
            setActive(isPowered());
        }
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        if (level != null) {
            onPowerChange(level);
        }
    }
}