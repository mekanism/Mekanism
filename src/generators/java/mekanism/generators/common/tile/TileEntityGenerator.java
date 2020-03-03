package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public abstract class TileEntityGenerator extends TileEntityMekanism {

    /**
     * Output per tick this generator can transfer.
     */
    public double output;

    /**
     * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
     */
    public TileEntityGenerator(IBlockProvider blockProvider, double out) {
        super(blockProvider);
        output = out;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            if (MekanismUtils.canFunction(this)) {
                CableUtils.emit(this);
            }
        }
    }

    @Override
    public double getMaxOutput() {
        return output;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return side == getDirection();
    }

    /**
     * Whether or not this generator can operate.
     *
     * @return if the generator can operate
     */
    public abstract boolean canOperate();

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}