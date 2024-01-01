package mekanism.common.recipe.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.maven.artifact.versioning.ComparableVersion;

public record ModVersionLoadedCondition(String modid, String minVersion) implements ICondition {

    @Override
    public boolean test(IContext context) {
        //They match or we are ahead of the min version
        return ModList.get().getModContainerById(modid).filter(modContainer -> new ComparableVersion(minVersion).compareTo(
              new ComparableVersion(modContainer.getModInfo().getVersion().toString())) <= 0).isPresent();
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return MekanismRecipeConditions.MOD_VERSION_LOADED.get();
    }

    public static Codec<ModVersionLoadedCondition> makeCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
              Codec.STRING.fieldOf("modid").forGetter(ModVersionLoadedCondition::modid),
              Codec.STRING.fieldOf("minVersion").forGetter(ModVersionLoadedCondition::minVersion)
        ).apply(instance, ModVersionLoadedCondition::new));
    }
}