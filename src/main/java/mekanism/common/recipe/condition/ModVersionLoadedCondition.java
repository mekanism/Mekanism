package mekanism.common.recipe.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.maven.artifact.versioning.ComparableVersion;

public record ModVersionLoadedCondition(String modid, String minVersion) implements ICondition {

    @Override
    public boolean test(IContext context) {
        //They match or we are ahead of the min version
        Optional<? extends ModContainer> containerById = ModList.get().getModContainerById(modid);
        if (containerById.isEmpty()) {
            return false;
        }
        ModContainer modContainer = containerById.get();
        return new ComparableVersion(minVersion).compareTo(new ComparableVersion(modContainer.getModInfo().getVersion().toString())) <= 0;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return MekanismRecipeConditions.MOD_VERSION_LOADED.get();
    }

    public static MapCodec<ModVersionLoadedCondition> makeCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Codec.STRING.fieldOf(SerializationConstants.MODID).forGetter(ModVersionLoadedCondition::modid),
              Codec.STRING.fieldOf(SerializationConstants.VERSION).forGetter(ModVersionLoadedCondition::minVersion)
        ).apply(instance, ModVersionLoadedCondition::new));
    }
}