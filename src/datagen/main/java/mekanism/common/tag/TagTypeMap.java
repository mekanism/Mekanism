package mekanism.common.tag;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class TagTypeMap<TYPE extends IForgeRegistryEntry<TYPE>> {

    private final Map<INamedTag<TYPE>, Tag.Builder> tagToBuilder = new Object2ObjectLinkedOpenHashMap<>();

    private final TagType<TYPE> tagType;

    public TagTypeMap(TagType<TYPE> tagType) {
        this.tagType = tagType;
    }

    public TagType<TYPE> getTagType() {
        return tagType;
    }

    public ITag.Builder getBuilder(INamedTag<TYPE> tag) {
        return tagToBuilder.computeIfAbsent(tag, ignored -> Tag.Builder.create());
    }

    public boolean isEmpty() {
        return tagToBuilder.isEmpty();
    }

    public void clear() {
        tagToBuilder.clear();
    }

    public Map<ResourceLocation, Tag.Builder> getBuilders() {
        return tagToBuilder.entrySet().stream().collect(Collectors.toMap(tag -> tag.getKey().getId(), Entry::getValue));
    }
}