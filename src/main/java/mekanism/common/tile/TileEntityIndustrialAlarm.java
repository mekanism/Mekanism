package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIndustrialAlarm extends TileEntityMekanism {

    public TileEntityIndustrialAlarm(BlockPos pos, BlockState state) {
        super(MekanismBlocks.INDUSTRIAL_ALARM, pos, state);
        delaySupplier = () -> 3;
        this.onPowerChange();
    }

    @Override
    public void onPowerChange() {
        super.onPowerChange();
        if (getLevel() != null && !getLevel().isClientSide()) {
            setActive(isPowered());
        }
    }
}