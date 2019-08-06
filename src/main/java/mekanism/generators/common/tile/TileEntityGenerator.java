package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockProvider;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityGenerator extends TileEntityMekanism implements IComputerIntegration, ISecurityTile {

    /**
     * Output per tick this generator can transfer.
     */
    public double output;

    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

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
            if (MekanismConfig.current().general.destroyDisabledBlocks.val()) {
                Block block = getBlockType();
                if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
                    //TODO: Better way of doing name?
                    Mekanism.logger.info("Destroying generator of type '" + block.getClass().getSimpleName() + "' at coords " + Coord4D.get(this) + " as according to config.");
                    world.setBlockToAir(getPos());
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
    public boolean canReceiveEnergy(EnumFacing side) {
        return false;
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return side == getDirection();
    }

    /**
     * Whether or not this generator can operate.
     *
     * @return if the generator can operate
     */
    public abstract boolean canOperate();

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
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

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
}