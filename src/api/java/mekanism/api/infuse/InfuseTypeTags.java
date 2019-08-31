package mekanism.api.infuse;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public class InfuseTypeTags {

    private static TagCollection<InfuseType> collection = new TagCollection<>(location -> Optional.empty(), "", false, "");
    private static int generation;

    public static void setCollection(TagCollection<InfuseType> collectionIn) {
        collection = collectionIn;
        ++generation;
    }

    public static TagCollection<InfuseType> getCollection() {
        return collection;
    }

    public static int getGeneration() {
        return generation;
    }

    public static class Wrapper extends Tag<InfuseType> {

        private int lastKnownGeneration = -1;
        private Tag<InfuseType> cachedTag;

        public Wrapper(ResourceLocation resourceLocationIn) {
            super(resourceLocationIn);
        }

        @Override
        public boolean contains(@Nonnull InfuseType gas) {
            if (this.lastKnownGeneration != InfuseTypeTags.generation) {
                this.cachedTag = InfuseTypeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = InfuseTypeTags.generation;
            }
            return this.cachedTag.contains(gas);
        }

        @Nonnull
        @Override
        public Collection<InfuseType> getAllElements() {
            if (this.lastKnownGeneration != InfuseTypeTags.generation) {
                this.cachedTag = InfuseTypeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = InfuseTypeTags.generation;
            }
            return this.cachedTag.getAllElements();
        }

        @Nonnull
        @Override
        public Collection<ITagEntry<InfuseType>> getEntries() {
            if (this.lastKnownGeneration != InfuseTypeTags.generation) {
                this.cachedTag = InfuseTypeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = InfuseTypeTags.generation;
            }
            return this.cachedTag.getEntries();
        }
    }
}