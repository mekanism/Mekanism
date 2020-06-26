package mekanism.api.chemical;

import java.util.Set;
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

    //TODO - 1.16: Evaluate TagRegistry#func_232932_a_ (client side only??)
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
        return getCollection().func_232975_b_(tag);
    }

    //TODO - 1.16: Figure out what this should be called
    public Set<ResourceLocation> func_232892_b_(TagCollection<CHEMICAL> collection) {
        return this.collection.func_232940_b_(collection);
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