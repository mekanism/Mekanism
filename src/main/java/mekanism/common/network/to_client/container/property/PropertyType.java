package mekanism.common.network.to_client.container.property;

import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBlockPos;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableByteArray;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLargeResourceStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableResource;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.network.to_client.container.property.resource.ChemicalResourcePropertyData;
import mekanism.common.network.to_client.container.property.resource.FluidResourcePropertyData;
import mekanism.common.network.to_client.container.property.resource.ItemResourcePropertyData;
import mekanism.common.network.to_client.container.property.resource.ResourceStackPropertyData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public enum PropertyType {
    BOOLEAN(Boolean.TYPE, false, (getter, setter, _) -> SyncableBoolean.create(() -> (boolean) getter.get(), setter::accept), () -> BooleanPropertyData.STREAM_CODEC),
    BYTE(Byte.TYPE, (byte) 0, (getter, setter, _) -> SyncableByte.create(() -> (byte) getter.get(), setter::accept), () -> BytePropertyData.STREAM_CODEC),
    DOUBLE(Double.TYPE, 0D, (getter, setter, _) -> SyncableDouble.create(() -> (double) getter.get(), setter::accept), () -> DoublePropertyData.STREAM_CODEC),
    FLOAT(Float.TYPE, 0F, (getter, setter, _) -> SyncableFloat.create(() -> (float) getter.get(), setter::accept), () -> FloatPropertyData.STREAM_CODEC),
    INT(Integer.TYPE, 0, (getter, setter, _) -> SyncableInt.create(() -> (int) getter.get(), setter::accept), () -> IntPropertyData.STREAM_CODEC),
    LONG(Long.TYPE, 0L, (getter, setter, _) -> SyncableLong.create(() -> (long) getter.get(), setter::accept), () -> LongPropertyData.STREAM_CODEC),
    SHORT(Short.TYPE, (short) 0, (getter, setter, _) -> SyncableShort.create(() -> (short) getter.get(), setter::accept), () -> ShortPropertyData.STREAM_CODEC),
    BYTE_ARRAY(byte[].class, new byte[0], (getter, setter, _) -> SyncableByteArray.create(() -> (byte[]) getter.get(), setter::accept), () -> ByteArrayPropertyData.STREAM_CODEC),
    ITEM_TYPE(ItemResource.class, ItemResource.EMPTY, (getter, setter, _) -> SyncableResource.createItem(() -> (ItemResource) getter.get(), setter::accept), () -> ItemResourcePropertyData.STREAM_CODEC),
    FLUID_TYPE(FluidResource.class, FluidResource.EMPTY, (getter, setter, _) -> SyncableResource.createFluid(() -> (FluidResource) getter.get(), setter::accept), () -> FluidResourcePropertyData.STREAM_CODEC),
    CHEMICAL_TYPE(ChemicalResource.class, ChemicalResource.EMPTY, (getter, setter, _) -> SyncableResource.createChemical(() -> (ChemicalResource) getter.get(), setter::accept), () -> ChemicalResourcePropertyData.STREAM_CODEC),
    ITEM_STACK(ItemStack.class, ItemStack.EMPTY, (getter, setter, _) -> SyncableItemStack.create(() -> (ItemStack) getter.get(), setter::accept), () -> ItemStackPropertyData.STREAM_CODEC),
    LARGE_RESOURCE_STACK(LargeResourceStack.class, null, SyncableLargeResourceStack::forSyncableProperty, () -> ResourceStackPropertyData.STREAM_CODEC),
    BLOCK_POS(BlockPos.class, null, (getter, setter, _) -> SyncableBlockPos.create(() -> (BlockPos) getter.get(), setter::accept), () -> BlockPosPropertyData.STREAM_CODEC);

    public static final IntFunction<PropertyType> BY_ID = ByIdMap.continuous(PropertyType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, PropertyType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PropertyType::ordinal);

    private final Class<?> type;
    @Nullable
    private final Object defaultValue;
    @Nullable
    private final CreatorFunction creatorFunction;
    private final Supplier<StreamCodec<? super RegistryFriendlyByteBuf, ? extends PropertyData>> streamCodec;

    private static final PropertyType[] VALUES = values();

    PropertyType(Class<?> type, @Nullable Object defaultValue, @Nullable CreatorFunction creatorFunction, Supplier<StreamCodec<? super RegistryFriendlyByteBuf, ? extends PropertyData>> streamCodec) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.creatorFunction = creatorFunction;
        this.streamCodec = streamCodec;
    }

    @Nullable
    public Object getDefault() {
        return defaultValue;
    }

    @Nullable
    public static PropertyType getFromType(Class<?> type) {
        for (PropertyType propertyType : VALUES) {
            if (type == propertyType.type) {
                return propertyType;
            }
        }
        return null;
    }

    public ISyncableData create(Supplier<Object> supplier, Consumer<Object> consumer, @Nullable Object defaultValue) {
        if (creatorFunction == null) {
            throw new IllegalStateException(name() + " does not support annotation based syncing.");
        }
        return creatorFunction.create(supplier, consumer, defaultValue);
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends PropertyData> streamCodec() {
        return streamCodec.get();
    }

    @FunctionalInterface
    private interface CreatorFunction {

        ISyncableData create(Supplier<Object> getter, Consumer<Object> setter, @Nullable Object defaultValue);
    }
}