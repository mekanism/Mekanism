package mekanism.common.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Information pertaining to the tags that get managed by the {@link MekanismTagManager}
 */
@SuppressWarnings("unused")
public final class ManagedTagType<TYPE extends IForgeRegistryEntry<TYPE>> {

    private static final List<ManagedTagType<?>> managedTypes = new ArrayList<>();
    private static final ManagedTagType<Gas> GAS = new ManagedTagType<>("gas", "gases", MekanismAPI::gasRegistry, ChemicalTags.GAS::setCollection);
    private static final ManagedTagType<InfuseType> INFUSE_TYPE = new ManagedTagType<>("infuse_type", "infuse_types", MekanismAPI::infuseTypeRegistry, ChemicalTags.INFUSE_TYPE::setCollection);
    private static final ManagedTagType<Pigment> PIGMENT = new ManagedTagType<>("pigment", "pigments", MekanismAPI::pigmentRegistry, ChemicalTags.PIGMENT::setCollection);
    private static final ManagedTagType<Slurry> SLURRY = new ManagedTagType<>("slurry", "slurries", MekanismAPI::slurryRegistry, ChemicalTags.SLURRY::setCollection);

    public static List<ManagedTagType<?>> getManagedTypes() {
        return managedTypes;
    }

    private final Consumer<TagCollection<TYPE>> collectionSetter;
    private final NonNullLazy<IForgeRegistry<TYPE>> registry;
    private final String type;
    private final String path;

    private ManagedTagType(String type, String path, NonNullLazy<IForgeRegistry<TYPE>> registry, Consumer<TagCollection<TYPE>> collectionSetter) {
        this.type = type;
        this.path = "tags/" + path;
        this.collectionSetter = collectionSetter;
        this.registry = registry;
        //Keep track of all types that exist
        managedTypes.add(this);
    }

    public ForgeRegistryTagCollection<TYPE> getTagCollection() {
        return new ForgeRegistryTagCollection<>(registry.get(), path, type, collectionSetter);
    }
}