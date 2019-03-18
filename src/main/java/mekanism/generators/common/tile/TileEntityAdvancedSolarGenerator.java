package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock,
      IEvaporationSolar {

    public TileEntityAdvancedSolarGenerator() {
        super("AdvancedSolarGenerator", 200000, generators.advancedSolarGeneration * 2);
    }

    @Override
    public boolean sideIsOutput(EnumFacing side) {
        return side == facing;
    }

    @Override
    protected float getConfiguredMax() {
        return (float)generators.advancedSolarGeneration;
    }

    @Override
    public void onPlace() {
        Coord4D current = Coord4D.get(this);
        MekanismUtils.makeBoundingBlock(world, getPos().add(0, 1, 0), current);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                MekanismUtils.makeBoundingBlock(world, getPos().add(x, 2, z), current);
            }
        }
    }

    @Override
    public void onBreak() {
        world.setBlockToAir(getPos().add(0, 1, 0));

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.setBlockToAir(getPos().add(x, 2, z));
            }
        }

        invalidate();
        world.setBlockToAir(getPos());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }
}
