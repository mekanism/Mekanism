package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IBlockProvider;
import mekanism.api.block.IBlockDisableable;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileEntityGenerator extends TileEntityMekanism implements IComputerIntegration {

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
        if (!world.isRemote) {
            if (MekanismConfig.general.destroyDisabledBlocks.get()) {
                Block block = getBlockType();
                if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
                    //TODO: Better way of doing name?
                    Mekanism.logger.info("Destroying generator of type '" + block.getClass().getSimpleName() + "' at coords " + Coord4D.get(this) + " as according to config.");
                    world.removeBlock(getPos(), false);
                    return;
                }
            }
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
    @OnlyIn(Dist.CLIENT)
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