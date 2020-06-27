package mekanism.common.tag;

import java.util.Arrays;
import java.util.Collection;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

//Based off of TagsProvider.Builder
public class ForgeRegistryTagBuilder<TYPE extends IForgeRegistryEntry<TYPE>> {

    private final ITag.Builder builder;
    private final String modID;

    public ForgeRegistryTagBuilder(ITag.Builder builder, String modID) {
        this.builder = builder;
        this.modID = modID;
    }

    public ForgeRegistryTagBuilder<TYPE> add(TYPE element) {
        this.builder.func_232961_a_(element.getRegistryName(), modID);
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
        this.builder.func_232964_b_(tag.func_230234_a_(), modID);
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
        builder.func_232955_a_(tag, modID);
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> replace() {
        return replace(true);
    }

    public ForgeRegistryTagBuilder<TYPE> replace(boolean value) {
        builder.replace(value);
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> addOptional(final ResourceLocation... locations) {
        return addOptional(Arrays.asList(locations));
    }

    public ForgeRegistryTagBuilder<TYPE> addOptional(final Collection<ResourceLocation> locations) {
        return add(ForgeHooks.makeOptionalTag(true, locations));
    }

    public ForgeRegistryTagBuilder<TYPE> addOptionalTag(final ResourceLocation... locations) {
        return addOptionalTag(Arrays.asList(locations));
    }

    public ForgeRegistryTagBuilder<TYPE> addOptionalTag(final Collection<ResourceLocation> locations) {
        return add(ForgeHooks.makeOptionalTag(false, locations));
    }
}