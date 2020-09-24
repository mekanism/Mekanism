package mekanism.api.chemical;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.registries.IForgeRegistry;

public class ChemicalTags<CHEMICAL extends Chemical<CHEMICAL>> {

    public static final ChemicalTags<Gas> GAS = new ChemicalTags<>(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "gas"), MekanismAPI::gasRegistry);
    public static final ChemicalTags<InfuseType> INFUSE_TYPE = new ChemicalTags<>(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infuse_type"), MekanismAPI::infuseTypeRegistry);
    public static final ChemicalTags<Pigment> PIGMENT = new ChemicalTags<>(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment"), MekanismAPI::pigmentRegistry);
    public static final ChemicalTags<Slurry> SLURRY = new ChemicalTags<>(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "slurry"), MekanismAPI::slurryRegistry);

    private final Supplier<IForgeRegistry<CHEMICAL>> registrySupplier;
    private final ResourceLocation registryName;

    private ChemicalTags(ResourceLocation registryName, Supplier<IForgeRegistry<CHEMICAL>> registrySupplier) {
        this.registrySupplier = registrySupplier;
        this.registryName = registryName;
    }

    public ITagCollection<CHEMICAL> getCollection() {
        IForgeRegistry<CHEMICAL> registry = registrySupplier.get();
        if (registry == null) {
            return (ITagCollection<CHEMICAL>) TagCollectionManager.getManager().getCustomTypeCollection(registryName);
        }
        return TagCollectionManager.getManager().getCustomTypeCollection(registry);
    }

    public ResourceLocation lookupTag(ITag<CHEMICAL> tag) {
        //Manual and slightly modified implementation of TagCollection#getDirectIdFromTag to have better reverse lookup handling
        ITagCollection<CHEMICAL> collection = getCollection();
        ResourceLocation resourceLocation = collection.getDirectIdFromTag(tag);
        if (resourceLocation == null) {
            //If we failed to get the resource location, try manually looking it up by a "matching" entry
            // as the objects are different and neither Tag nor NamedTag override equals and hashCode
            List<CHEMICAL> chemicals = tag.getAllElements();
            for (Entry<ResourceLocation, ITag<CHEMICAL>> entry : collection.getIDTagMap().entrySet()) {
                if (chemicals.equals(entry.getValue().getAllElements())) {
                    resourceLocation = entry.getKey();
                    break;
                }
            }
        }
        if (resourceLocation == null) {
            throw new IllegalStateException("Unrecognized tag");
        }
        return resourceLocation;
    }

    public INamedTag<CHEMICAL> tag(ResourceLocation name) {
        IForgeRegistry<CHEMICAL> registry = registrySupplier.get();
        if (registry == null) {
            return ForgeTagHandler.makeWrapperTag(registryName, name);
        }
        return ForgeTagHandler.makeWrapperTag(registry, name);
    }

    public IOptionalNamedTag<CHEMICAL> optionalTag(ResourceLocation name) {
        return optionalTag(name, null);
    }

    public IOptionalNamedTag<CHEMICAL> optionalTag(ResourceLocation name, @Nullable Set<Supplier<CHEMICAL>> defaults) {
        IForgeRegistry<CHEMICAL> registry = registrySupplier.get();
        if (registry == null) {
            return ForgeTagHandler.createOptionalTag(registryName, name, defaults);
        }
        return ForgeTagHandler.createOptionalTag(registry, name, defaults);
    }
}