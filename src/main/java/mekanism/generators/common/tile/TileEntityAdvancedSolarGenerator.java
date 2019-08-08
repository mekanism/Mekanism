package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar {

    public TileEntityAdvancedSolarGenerator() {
        super(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR, MekanismConfig.current().generators.advancedSolarGeneration.val() * 2);
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return side == getDirection();
    }

    @Override
    protected float getConfiguredMax() {
        return (float) MekanismConfig.current().generators.advancedSolarGeneration.val();
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
        world.removeBlock(getPos().add(0, 1, 0), false);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.removeBlock(getPos().add(x, 2, z), false);
            }
        }
        invalidate();
        world.removeBlock(getPos(), false);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        return capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY) {
            return Capabilities.EVAPORATION_SOLAR_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean canSeeSky() {
        return world.canBlockSeeSky(getPos().up(3));
    }
}