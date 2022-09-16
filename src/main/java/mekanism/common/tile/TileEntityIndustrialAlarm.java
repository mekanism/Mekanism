package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public AABB getRenderBoundingBox() {
        if (getActive()) {
            //If it is active allow the full block to be rendered rather than just the model's shape as we want to allow the aura to render
            return new AABB(worldPosition, worldPosition.offset(1, 1, 1));
        }
        return super.getRenderBoundingBox();
    }
}