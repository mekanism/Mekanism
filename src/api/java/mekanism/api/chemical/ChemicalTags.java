package mekanism.api.chemical;

import java.util.List;
import java.util.Map.Entry;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagRegistry;
import net.minecraft.util.ResourceLocation;

public class ChemicalTags<CHEMICAL extends Chemical<CHEMICAL>> {

    public static final ChemicalTags<Gas> GAS = new ChemicalTags<>();
    public static final ChemicalTags<InfuseType> INFUSE_TYPE = new ChemicalTags<>();
    public static final ChemicalTags<Pigment> PIGMENT = new ChemicalTags<>();
    public static final ChemicalTags<Slurry> SLURRY = new ChemicalTags<>();

    private final TagRegistry<CHEMICAL> collection = new TagRegistry<>();

    private ChemicalTags() {
    }

    public void setCollection(TagCollection<CHEMICAL> collectionIn) {
        collection.func_232935_a_(collectionIn);
    }

    public TagCollection<CHEMICAL> getCollection() {
        return collection.func_232939_b_();
    }

    public ResourceLocation lookupTag(ITag<CHEMICAL> tag) {
        //Manual and slightly modified implementation of TagCollection#func_232975_b_ to have better reverse lookup handling
        TagCollection<CHEMICAL> collection = getCollection();
        ResourceLocation resourceLocation = collection.func_232973_a_(tag);
        if (resourceLocation == null) {
            //If we failed to get the resource location, try manually looking it up by a "matching" entry
            // as the objects are different and neither Tag nor NamedTag override equals and hashCode
            List<CHEMICAL> chemicals = tag.func_230236_b_();
            for (Entry<ResourceLocation, ITag<CHEMICAL>> entry : collection.getTagMap().entrySet()) {
                if (chemicals.equals(entry.getValue().func_230236_b_())) {
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

    public static INamedTag<Gas> gasTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, GAS);
    }

    public static INamedTag<InfuseType> infusionTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, INFUSE_TYPE);
    }

    public static INamedTag<Pigment> pigmentTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, PIGMENT);
    }

    public static INamedTag<Slurry> slurryTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, SLURRY);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> INamedTag<CHEMICAL> chemicalTag(ResourceLocation resourceLocation, ChemicalTags<CHEMICAL> chemicalTags) {
        return chemicalTags.collection.func_232937_a_(resourceLocation.toString());
    }
}