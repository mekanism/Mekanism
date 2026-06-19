package mekanism.client.texture;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

public abstract class BaseSpriteSourceProvider extends SpriteSourceProvider {

    private final Set<Identifier> trackedSingles = new HashSet<>();

    protected BaseSpriteSourceProvider(PackOutput output, String modid, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider, modid);
    }

    protected void addFiles(SourceList atlas, List<Identifier> resourceLocations) {
        addFiles(atlas, resourceLocations.stream().sorted(Identifier::compareNamespaced).toArray(Identifier[]::new));
    }

    protected void addFiles(SourceList atlas, Identifier... resourceLocations) {
        for (Identifier rl : resourceLocations) {
            //Only add this source if we haven't already added it as a direct single file source
            if (trackedSingles.add(rl)) {
                atlas.addSource(new SingleFile(rl, Optional.empty()));
            }
        }
    }

    protected void addDirectory(SourceList atlas, String directory, String spritePrefix) {
        atlas.addSource(new DirectoryLister(directory, spritePrefix));
    }
}