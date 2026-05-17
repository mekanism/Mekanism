package mekanism.common.network.to_client.container.property.resource;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

@NothingNullByDefault
public final class ResourceStackPropertyData extends PropertyData {

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          StreamCodec.of(ResourceType::encode, ResourceType::decode), data -> data.value,
          ResourceStackPropertyData::new
    );

    private final LargeResourceStack<?> value;

    public ResourceStackPropertyData(short property, LargeResourceStack<?> value) {
        super(PropertyType.LARGE_RESOURCE_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    private enum ResourceType {
        ITEM(SerializerHelper.ITEM_RESOURCE_STACK_STREAM_CODEC),
        FLUID(SerializerHelper.FLUID_RESOURCE_STACK_STREAM_CODEC),
        CHEMICAL(SerializerHelper.CHEMICAL_RESOURCE_STACK_STREAM_CODEC);

        public static final IntFunction<ResourceType> BY_ID = ByIdMap.continuous(ResourceType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ResourceType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ResourceType::ordinal);

        private final StreamCodec<RegistryFriendlyByteBuf, ? extends LargeResourceStack<?>> streamCodec;

        <RESOURCE extends Resource> ResourceType(StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> streamCodec) {
            this.streamCodec = streamCodec;
        }

        public static LargeResourceStack<?> decode(RegistryFriendlyByteBuf buf) {
            return STREAM_CODEC.decode(buf).streamCodec.decode(buf);
        }

        //TODO - 26.1: Re-evaluate this
        public static <RESOURCE extends Resource> void encode(RegistryFriendlyByteBuf buf, LargeResourceStack<RESOURCE> stack) {
            ResourceType type = switch (stack.resource()) {
                case ItemResource _ -> ITEM;
                case FluidResource _ -> FLUID;
                case ChemicalResource _ -> CHEMICAL;
                default -> throw new IllegalArgumentException("Unknown resource type: " + stack.resource());
            };
            STREAM_CODEC.encode(buf, type);
            ((StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>>) type.streamCodec).encode(buf, stack);
        }
    }
}