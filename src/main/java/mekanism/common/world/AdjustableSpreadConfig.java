package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.resource.OreType;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class AdjustableSpreadConfig implements IPlacementConfig, IFeatureConfig {

    public static final Codec<AdjustableSpreadConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          OreType.CODEC.optionalFieldOf("oreType").forGetter(config -> Optional.ofNullable(config.oreType))
    ).apply(builder, oreType -> {
        if (oreType.isPresent()) {
            OreType type = oreType.get();
            return new AdjustableSpreadConfig(type, MekanismConfig.world.ores.get(type).perChunk);
        } else {
            return new AdjustableSpreadConfig(null, MekanismConfig.world.salt.perChunk);
        }
    }));

    private final IntSupplier spread;
    @Nullable
    private final OreType oreType;

    public AdjustableSpreadConfig(@Nullable OreType oreType, IntSupplier spread) {
        this.oreType = oreType;
        this.spread = spread;
    }

    public int getSpread(Random rand) {
        int spread = this.spread.getAsInt();
        return spread == 0 ? 0 : rand.nextInt(spread + 1);
    }
}