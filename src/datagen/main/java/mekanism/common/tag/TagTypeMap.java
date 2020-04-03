package mekanism.common.tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class TagTypeMap<TYPE extends IForgeRegistryEntry<TYPE>> {

    private final Map<Tag<TYPE>, Tag.Builder<TYPE>> tagToBuilder = new Object2ObjectLinkedOpenHashMap<>();

    private final TagType<TYPE> tagType;

    public TagTypeMap(TagType<TYPE> tagType) {
        this.tagType = tagType;
    }

    public TagType<TYPE> getTagType() {
        return tagType;
    }

    public Tag.Builder<TYPE> getBuilder(Tag<TYPE> tag) {
        return tagToBuilder.computeIfAbsent(tag, ignored -> Tag.Builder.create());
    }

    public boolean isEmpty() {
        return tagToBuilder.isEmpty();
    }

    public void clear() {
        tagToBuilder.clear();
    }

    public Map<ResourceLocation, Tag.Builder<TYPE>> getBuilders() {
        return tagToBuilder.entrySet().stream().collect(Collectors.toMap(tag -> tag.getKey().getId(), Entry::getValue));
    }
}