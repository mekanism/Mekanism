package mekanism.additions.common.world_modifier;

import java.util.function.BiConsumer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.common.world_modifier.BaseBiomeModifierProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.BiomeModifier;

public class AdditionsBiomeModifierProvider extends BaseBiomeModifierProvider {

    public AdditionsBiomeModifierProvider(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void getModifiers(RegistryGetter registryGetter, BiConsumer<BiomeModifier, ResourceLocation> consumer) {
        for (BabyType babyType : BabyType.values()) {
            consumer.accept(new BabyEntitySpawnBiomeModifier(babyType), MekanismAdditions.rl(babyType.getSerializedName()));
        }
    }
}