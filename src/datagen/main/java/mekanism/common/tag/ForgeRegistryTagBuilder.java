package mekanism.common.tag;

import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

//Based off of TagsProvider.Builder but with a few shortcuts for forge registry entries and also a few more helpers and addition of SafeVarargs annotations
public class ForgeRegistryTagBuilder<TYPE extends IForgeRegistryEntry<TYPE>> {

    private final ITag.Builder builder;
    private final String modID;

    public ForgeRegistryTagBuilder(ITag.Builder builder, String modID) {
        this.builder = builder;
        this.modID = modID;
    }

    public ForgeRegistryTagBuilder<TYPE> add(TYPE element) {
        this.builder.addItemEntry(element.getRegistryName(), modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(TYPE... elements) {
        for (TYPE element : elements) {
            add(element);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(INamedTag<TYPE> tag) {
        this.builder.addTagEntry(tag.getName(), modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(INamedTag<TYPE>... tags) {
        for (INamedTag<TYPE> tag : tags) {
            add(tag);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(ITag.ITagEntry tag) {
        builder.addTag(tag, modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(RegistryKey<TYPE>... keys) {
        for (RegistryKey<TYPE> key : keys) {
            builder.addItemEntry(key.getLocation(), modID);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> replace() {
        return replace(true);
    }

    public ForgeRegistryTagBuilder<TYPE> replace(boolean value) {
        builder.replace(value);
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> addOptional(ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            add(new ITag.OptionalItemEntry(location));
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> addOptionalTag(ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            add(new ITag.OptionalTagEntry(location));
        }
        return this;
    }
}