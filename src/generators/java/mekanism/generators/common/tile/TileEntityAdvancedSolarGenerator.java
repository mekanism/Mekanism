package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar {

    public TileEntityAdvancedSolarGenerator() {
        super(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR, MekanismConfigOld.current().generators.advancedSolarGeneration.get() * 2);
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return side == getDirection();
    }

    @Override
    protected float getConfiguredMax() {
        return (float) MekanismConfigOld.current().generators.advancedSolarGeneration.get();
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
        remove();
        world.removeBlock(getPos(), false);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY) {
            return Capabilities.EVAPORATION_SOLAR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean canSeeSky() {
        return world.canBlockSeeSky(getPos().up(3));
    }
}