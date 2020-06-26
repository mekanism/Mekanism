package mekanism.common.tags;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Builder;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ForgeRegistryTagCollection<T extends IForgeRegistryEntry<T>> extends TagCollection<T> {

    private final IForgeRegistry<T> registry;
    private final Consumer<TagCollection<T>> collectionSetter;

    public ForgeRegistryTagCollection(IForgeRegistry<T> registry, String location, String type, Consumer<TagCollection<T>> collectionSetter) {
        super(key -> Optional.ofNullable(registry.getValue(key)), location, false, type);
        this.registry = registry;
        this.collectionSetter = collectionSetter;
    }

    public void write(PacketBuffer buffer) {
        Map<ResourceLocation, ITag<T>> tagMap = this.getTagMap();
        buffer.writeVarInt(tagMap.size());
        for (Entry<ResourceLocation, ITag<T>> entry : tagMap.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            ITag<T> tag = entry.getValue();
            List<T> tags = tag.func_230236_b_();
            buffer.writeVarInt(tags.size());
            for (T element : tags) {
                ResourceLocation key = this.registry.getKey(element);
                if (key != null) {
                    buffer.writeResourceLocation(key);
                }
            }
        }
    }

    public void read(PacketBuffer buffer) {
        Map<ResourceLocation, ITag<T>> tagMap = new Object2ObjectOpenHashMap<>();
        int tagCount = buffer.readVarInt();
        for (int i = 0; i < tagCount; ++i) {
            ResourceLocation resourceLocation = buffer.readResourceLocation();
            int elementCount = buffer.readVarInt();
            Builder<T> builder = Builder.create();
            for (int j = 0; j < elementCount; ++j) {
                T value = registry.getValue(buffer.readResourceLocation());
                if (value != null) {
                    //Should never be null anyways
                    builder.add(value);
                }
            }
            tagMap.put(resourceLocation, builder.build(resourceLocation));
        }
        this.toImmutable(tagMap);
    }

    public void setCollection() {
        collectionSetter.accept(this);
    }
}