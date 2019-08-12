package mekanism.common.tags;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.common.MekanismBlock;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class MekanismBlockTagsProvider extends BlockTagsProvider {

    private Set<ResourceLocation> filter = null;

    public MekanismBlockTagsProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    public void registerTags() {
        super.registerTags();
        filter = this.tagToBuilder.keySet().stream().map(Tag::getId).collect(Collectors.toSet());

        //TODO: Generify to IBlockProvider and make it be a param of creating this class to simplify logic for mekanism generators
        for (MekanismBlock mekanismBlock : MekanismBlock.values()) {

        }

        //TODO: Also add things like plastic fences to the vanilla fences tag

        //getBuilder(BlockTag).add(BlockTag)....)
        //getBuilder(BlockTag).).add(Block..)
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        //We don't want to save vanilla tags.
        return filter != null && filter.contains(id) ? null : super.makePath(id);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Mekanism Block Tags";
    }
}