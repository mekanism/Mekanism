package mekanism.common.world;

import java.util.Random;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class DisableableConfiguredFeature<FC extends IFeatureConfig, F extends Feature<FC>> extends ConfiguredFeature<FC, F> {

    private final BooleanSupplier enabledSupplier;
    private final boolean retroGen;

    public DisableableConfiguredFeature(F feature, FC config, BooleanSupplier enabledSupplier, boolean retroGen) {
        super(feature, config);
        this.enabledSupplier = enabledSupplier;
        this.retroGen = retroGen;
    }

    @Override
    public boolean generate(@Nonnull ISeedReader reader, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Random rand, @Nonnull BlockPos pos) {
        if (enabledSupplier.getAsBoolean()) {
            //If we are enabled and we are either not a retrogen feature or retrogen is enabled, generate
            if (!retroGen || MekanismConfig.world.enableRegeneration.get()) {
                return super.generate(reader, chunkGenerator, rand, pos);
            }
        }
        return false;
    }
}