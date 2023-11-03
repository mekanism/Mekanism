package mekanism.common.recipe.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class ModVersionLoadedCondition implements ICondition {

    private final String minVersion;
    private final String modid;

    public ModVersionLoadedCondition(String modid, String version) {
        this.modid = modid;
        this.minVersion = version;
    }

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

    @Override
    public String toString() {
        return "mod_version_loaded(\"" + modid + "\", \"" + minVersion + "\")";
    }

    public static Codec<ModVersionLoadedCondition> makeCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("modid").forGetter(o->o.modid), Codec.STRING.fieldOf("minVersion").forGetter(o->o.minVersion)).apply(instance, ModVersionLoadedCondition::new));
    }
}