package mekanism.common.world;

import java.util.Random;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class DisableableConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> extends ConfiguredFeature<FC, F> {

    private final BooleanSupplier enabledSupplier;
    private final boolean retroGen;

    public DisableableConfiguredFeature(F feature, FC config, BooleanSupplier enabledSupplier, boolean retroGen) {
        super(feature, config);
        this.enabledSupplier = enabledSupplier;
        this.retroGen = retroGen;
    }

    @Override
    public boolean place(@Nonnull WorldGenLevel reader, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Random rand, @Nonnull BlockPos pos) {
        if (enabledSupplier.getAsBoolean()) {
            //If we are enabled, and we are either not a retrogen feature or retrogen is enabled, generate
            if (!retroGen || MekanismConfig.world.enableRegeneration.get()) {
                return super.place(reader, chunkGenerator, rand, pos);
            }
        }
        return false;
    }
}