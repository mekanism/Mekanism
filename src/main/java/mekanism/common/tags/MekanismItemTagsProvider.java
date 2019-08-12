package mekanism.common.tags;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.common.MekanismItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Items;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Potentially move this to a different package
public class MekanismItemTagsProvider extends ItemTagsProvider {

    private Set<ResourceLocation> filter = null;

    public MekanismItemTagsProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    public void registerTags() {
        super.registerTags();
        filter = this.tagToBuilder.keySet().stream().map(Tag::getId).collect(Collectors.toSet());

        //copy(BlockTag, ItemTag);
        //getBuilder(ItemTag).add(ItemTag...)
        //getBuilder(ItemTag).add(Item..)
        getBuilder(MekanismTags.Items.ALLOYS_BASIC).add(Items.REDSTONE);
        getBuilder(MekanismTags.Items.ALLOY_ENRICHED).add(MekanismTags.Items.ALLOYS_ADVANCED);
        getBuilder(MekanismTags.Items.ALLOYS_ADVANCED).add(MekanismItem.ENRICHED_ALLOY.getItem());
        getBuilder(MekanismTags.Items.ALLOYS_ELITE).add(MekanismItem.REINFORCED_ALLOY.getItem());
        getBuilder(MekanismTags.Items.ALLOYS_ULTIMATE).add(MekanismItem.ATOMIC_ALLOY.getItem());
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        //We don't want to save vanilla tags.
        return filter != null && filter.contains(id) ? null : super.makePath(id);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Mekanism Item Tags";
    }
}