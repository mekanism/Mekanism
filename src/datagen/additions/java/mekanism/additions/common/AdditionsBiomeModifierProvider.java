package mekanism.additions.common;

import java.util.function.BiConsumer;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.biome_modifier.BabyEntitySpawnBiomeModifier;
import mekanism.common.biome_modifier.BaseBiomeModifierProvider;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;

public class AdditionsBiomeModifierProvider extends BaseBiomeModifierProvider {

    public AdditionsBiomeModifierProvider(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void getModifiers(Registry<Biome> biomeRegistry, BiConsumer<BiomeModifier, ResourceLocation> consumer) {
        for (BabyType babyType : BabyType.values()) {
            consumer.accept(new BabyEntitySpawnBiomeModifier(babyType), MekanismAdditions.rl(babyType.getSerializedName()));
        }
    }
}