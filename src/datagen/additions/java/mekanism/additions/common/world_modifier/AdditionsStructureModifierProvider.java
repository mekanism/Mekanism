package mekanism.additions.common.world_modifier;

import java.util.function.BiConsumer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.world_modifier.BaseStructureModifierProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.StructureModifier;

public class AdditionsStructureModifierProvider extends BaseStructureModifierProvider {

    public AdditionsStructureModifierProvider(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void getModifiers(RegistryGetter registryGetter, BiConsumer<StructureModifier, ResourceLocation> consumer) {
        for (BabyType babyType : BabyType.values()) {
            consumer.accept(new BabyEntitySpawnStructureModifier(babyType), MekanismAdditions.rl(babyType.getSerializedName()));
        }
    }
}