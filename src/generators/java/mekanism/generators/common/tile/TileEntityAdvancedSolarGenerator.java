package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.IEvaporationSolar;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar {

    public TileEntityAdvancedSolarGenerator() {
        super(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, MekanismGeneratorsConfig.generators.advancedSolarGeneration.get().multiply(2));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.EVAPORATION_SOLAR_CAPABILITY, this));
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return new RelativeSide[]{RelativeSide.FRONT, RelativeSide.BOTTOM};
    }

    @Override
    protected FloatingLong getConfiguredMax() {
        return MekanismGeneratorsConfig.generators.advancedSolarGeneration.get();
    }

    @Override
    public void onPlace() {
        if (world != null) {
            BlockPos pos = getPos();
            WorldUtils.makeBoundingBlock(world, pos.add(0, 1, 0), pos);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    WorldUtils.makeBoundingBlock(world, pos.add(x, 2, z), pos);
                }
            }
        }
    }

    @Override
    public void onBreak(BlockState oldState) {
        if (world != null) {
            world.removeBlock(getPos().add(0, 1, 0), false);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    world.removeBlock(getPos().add(x, 2, z), false);
                }
            }
            remove();
            world.removeBlock(getPos(), false);
        }
    }

    @Override
    protected BlockPos getSkyCheckPos() {
        return pos.up(2);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 3, 2));
    }
}