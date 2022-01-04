package mekanism.common.tag;

import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

//Based off of TagsProvider.Builder but with a few shortcuts for forge registry entries and also a few more helpers and addition of SafeVarargs annotations
public class ForgeRegistryTagBuilder<TYPE extends IForgeRegistryEntry<TYPE>> {

    private final Tag.Builder builder;
    private final String modID;

    public ForgeRegistryTagBuilder(Tag.Builder builder, String modID) {
        this.builder = builder;
        this.modID = modID;
    }

    public ForgeRegistryTagBuilder<TYPE> add(TYPE element) {
        this.builder.addElement(element.getRegistryName(), modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(TYPE... elements) {
        for (TYPE element : elements) {
            add(element);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(Named<TYPE> tag) {
        this.builder.addTag(tag.getName(), modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(Named<TYPE>... tags) {
        for (Named<TYPE> tag : tags) {
            add(tag);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(Tag.Entry tag) {
        builder.add(tag, modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(ResourceKey<TYPE>... keys) {
        for (ResourceKey<TYPE> key : keys) {
            builder.addElement(key.location(), modID);
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
            add(new Tag.OptionalElementEntry(location));
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> addOptionalTag(ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            add(new Tag.OptionalTagEntry(location));
        }
        return this;
    }
}