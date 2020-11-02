package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.resource.OreType;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.template.RuleTest;

public class ResizableOreFeatureConfig implements IFeatureConfig {

    public static final Codec<ResizableOreFeatureConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          RuleTest.field_237127_c_.fieldOf("target").forGetter(config -> config.target),
          OreType.CODEC.fieldOf("oreType").forGetter(config -> config.oreType)
    ).apply(builder, ResizableOreFeatureConfig::create));

    private static ResizableOreFeatureConfig create(RuleTest target, OreType oreType) {
        return new ResizableOreFeatureConfig(target, oreType, MekanismConfig.world.ores.get(oreType).maxVeinSize);
    }

    public final BlockState state;
    public final RuleTest target;
    public final OreType oreType;
    public final IntSupplier size;

    public ResizableOreFeatureConfig(RuleTest target, OreType oreType, IntSupplier size) {
        this.target = target;
        this.oreType = oreType;
        this.size = size;
        this.state = MekanismBlocks.ORES.get(oreType).getBlock().getDefaultState();
    }
}