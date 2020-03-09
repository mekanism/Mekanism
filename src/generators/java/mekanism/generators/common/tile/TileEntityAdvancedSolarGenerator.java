package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IEvaporationSolar;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar {

    public TileEntityAdvancedSolarGenerator() {
        super(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, MekanismGeneratorsConfig.generators.advancedSolarGeneration.get() * 2);
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return side == getDirection();
    }

    @Override
    protected double getConfiguredMax() {
        return MekanismGeneratorsConfig.generators.advancedSolarGeneration.get();
    }

    @Override
    public void onPlace() {
        World world = getWorld();
        if (world == null) {
            return;
        }
        BlockPos pos = getPos();
        MekanismUtils.makeBoundingBlock(world, pos.add(0, 1, 0), pos);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                MekanismUtils.makeBoundingBlock(world, pos.add(x, 2, z), pos);
            }
        }
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world == null) {
            return;
        }
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
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.EVAPORATION_SOLAR_CAPABILITY) {
            return Capabilities.EVAPORATION_SOLAR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    protected boolean canSeeSky() {
        //TODO: Verify this is correct at 2, I for some reason had it at 3 before
        World world = getWorld();
        return world != null && world.canBlockSeeSky(getPos().up(2));
    }
}