package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.resource.OreType;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class ResizableTopSolidRangeConfig implements IPlacementConfig {

    public static final Codec<ResizableTopSolidRangeConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          OreType.CODEC.fieldOf("oreType").forGetter(config -> config.oreType)
    ).apply(builder, ResizableTopSolidRangeConfig::create));

    private static ResizableTopSolidRangeConfig create(OreType oreType) {
        return new ResizableTopSolidRangeConfig(oreType, MekanismConfig.world.ores.get(oreType));
    }

    private final OreType oreType;
    public final IntSupplier bottomOffset;
    public final IntSupplier topOffset;
    public final IntSupplier maximum;

    public ResizableTopSolidRangeConfig(OreType oreType, OreConfig oreConfig) {
        this.oreType = oreType;
        this.bottomOffset = oreConfig.bottomOffset;
        this.topOffset = oreConfig.topOffset;
        this.maximum = oreConfig.maxHeight;
    }
}