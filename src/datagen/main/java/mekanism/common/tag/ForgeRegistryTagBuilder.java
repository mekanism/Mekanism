package mekanism.common.tag;

import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.extensions.IForgeTagBuilder;
import net.minecraftforge.registries.IForgeRegistryEntry;

//Based off of TagsProvider.Builder
public class ForgeRegistryTagBuilder<TYPE extends IForgeRegistryEntry<TYPE>> implements IForgeTagBuilder<TYPE> {

    private final ITag.Builder builder;
    private final String modID;

    public ForgeRegistryTagBuilder(ITag.Builder builder, String modID) {
        this.builder = builder;
        this.modID = modID;
    }

    public ForgeRegistryTagBuilder<TYPE> add(TYPE element) {
        this.builder.func_232961_a_(element.getRegistryName(), this.modID);
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(INamedTag<TYPE> tag) {
        this.builder.func_232964_b_(tag.func_230234_a_(), this.modID);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(INamedTag<TYPE>... tags) {
        for (INamedTag<TYPE> tag : tags) {
            this.builder.func_232964_b_(tag.func_230234_a_(), this.modID);
        }
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(TYPE... elements) {
        for (TYPE element : elements) {
            this.builder.func_232961_a_(element.getRegistryName(), this.modID);
        }
        return this;
    }

    public ForgeRegistryTagBuilder<TYPE> add(ITag.ITagEntry tag) {
        builder.func_232955_a_(tag, modID);
        return this;
    }

    public ITag.Builder getInternalBuilder() {
        return builder;
    }

    public String getModID() {
        return modID;
    }
}