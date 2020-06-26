package mekanism.api.chemical;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

//TODO - 1.16: Figure out ITag versus INamedTag (Some things we moved to INamedTag maybe could use ITag)
public class ChemicalTags<CHEMICAL extends Chemical<CHEMICAL>> {

    public static final ChemicalTags<Gas> GAS = new ChemicalTags<>();
    public static final ChemicalTags<InfuseType> INFUSE_TYPE = new ChemicalTags<>();
    public static final ChemicalTags<Pigment> PIGMENT = new ChemicalTags<>();
    public static final ChemicalTags<Slurry> SLURRY = new ChemicalTags<>();

    private TagCollection<CHEMICAL> collection = new TagCollection<>(location -> Optional.empty(), "", "");
    private int generation;

    private ChemicalTags() {
    }

    public void setCollection(TagCollection<CHEMICAL> collectionIn) {
        collection = collectionIn;
        generation++;
    }

    public TagCollection<CHEMICAL> getCollection() {
        return collection;
    }

    public int getGeneration() {
        return generation;
    }

    public static Tag<Gas> gasTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, GAS);
    }

    public static Tag<InfuseType> infusionTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, INFUSE_TYPE);
    }

    public static Tag<Pigment> pigmentTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, PIGMENT);
    }

    public static Tag<Slurry> slurryTag(ResourceLocation resourceLocation) {
        return chemicalTag(resourceLocation, SLURRY);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> Tag<CHEMICAL> chemicalTag(ResourceLocation resourceLocation, ChemicalTags<CHEMICAL> chemicalTags) {
        return new ChemicalTag<>(resourceLocation, chemicalTags);
    }

    private static class ChemicalTag<CHEMICAL extends Chemical<CHEMICAL>> implements Tag<CHEMICAL> {

        private final ChemicalTags<CHEMICAL> chemicalTags;
        private int lastKnownGeneration = -1;
        private Tag<CHEMICAL> cachedTag;

        protected ChemicalTag(ResourceLocation resourceLocation, ChemicalTags<CHEMICAL> chemicalTags) {
            super(resourceLocation);
            this.chemicalTags = chemicalTags;
        }

        private void validateCache() {
            int generation = chemicalTags.getGeneration();
            if (this.lastKnownGeneration != generation) {
                this.cachedTag = chemicalTags.getCollection().getOrCreate(getId());
                this.lastKnownGeneration = generation;
            }
        }

        @Override
        public boolean contains(@Nonnull CHEMICAL chemical) {
            validateCache();
            return this.cachedTag.contains(chemical);
        }

        @Nonnull
        @Override
        public List<CHEMICAL> func_230236_b_() {
            validateCache();
            return this.cachedTag.func_230236_b_();
        }

        @Nonnull
        @Override
        public Collection<ITagEntry<CHEMICAL>> getEntries() {
            validateCache();
            return this.cachedTag.getEntries();
        }
    }
}