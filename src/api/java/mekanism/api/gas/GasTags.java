package mekanism.api.gas;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public class GasTags {

    private static TagCollection<Gas> collection = new TagCollection<>(location -> Optional.empty(), "", false, "");
    private static int generation;

    public static void setCollection(TagCollection<Gas> collectionIn) {
        collection = collectionIn;
        ++generation;
    }

    public static TagCollection<Gas> getCollection() {
        return collection;
    }

    public static int getGeneration() {
        return generation;
    }

    public static class Wrapper extends Tag<Gas> {

        private int lastKnownGeneration = -1;
        private Tag<Gas> cachedTag;

        public Wrapper(ResourceLocation resourceLocationIn) {
            super(resourceLocationIn);
        }

        @Override
        public boolean contains(@Nonnull Gas gas) {
            if (this.lastKnownGeneration != GasTags.generation) {
                this.cachedTag = GasTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = GasTags.generation;
            }
            return this.cachedTag.contains(gas);
        }

        @Nonnull
        @Override
        public Collection<Gas> getAllElements() {
            if (this.lastKnownGeneration != GasTags.generation) {
                this.cachedTag = GasTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = GasTags.generation;
            }
            return this.cachedTag.getAllElements();
        }

        @Nonnull
        @Override
        public Collection<Tag.ITagEntry<Gas>> getEntries() {
            if (this.lastKnownGeneration != GasTags.generation) {
                this.cachedTag = GasTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = GasTags.generation;
            }
            return this.cachedTag.getEntries();
        }
    }
}