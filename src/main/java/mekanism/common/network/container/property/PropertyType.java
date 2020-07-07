package mekanism.common.network.container.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBlockPos;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.inventory.container.sync.chemical.SyncableGasStack;
import mekanism.common.inventory.container.sync.chemical.SyncableInfusionStack;
import mekanism.common.inventory.container.sync.chemical.SyncablePigmentStack;
import mekanism.common.inventory.container.sync.chemical.SyncableSlurryStack;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.container.property.chemical.GasStackPropertyData;
import mekanism.common.network.container.property.chemical.InfusionStackPropertyData;
import mekanism.common.network.container.property.chemical.PigmentStackPropertyData;
import mekanism.common.network.container.property.chemical.SlurryStackPropertyData;
import mekanism.common.network.container.property.list.ListPropertyData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public enum PropertyType {
    BOOLEAN(Boolean.TYPE, false, (getter, setter) -> SyncableBoolean.create(() -> (boolean) getter.get(), setter::accept),
          (property, buffer) -> new BooleanPropertyData(property, buffer.readBoolean())),
    BYTE(Byte.TYPE, (byte) 0, (getter, setter) -> SyncableByte.create(() -> (byte) getter.get(), setter::accept),
          (property, buffer) -> new BytePropertyData(property, buffer.readByte())),
    DOUBLE(Double.TYPE, 0D, (getter, setter) -> SyncableDouble.create(() -> (double) getter.get(), setter::accept),
          (property, buffer) -> new DoublePropertyData(property, buffer.readDouble())),
    FLOAT(Float.TYPE, 0F, (getter, setter) -> SyncableFloat.create(() -> (float) getter.get(), setter::accept),
          (property, buffer) -> new FloatPropertyData(property, buffer.readFloat())),
    INT(Integer.TYPE, 0, (getter, setter) -> SyncableInt.create(() -> (int) getter.get(), setter::accept),
          (property, buffer) -> new IntPropertyData(property, buffer.readVarInt())),
    LONG(Long.TYPE, 0L, (getter, setter) -> SyncableLong.create(() -> (long) getter.get(), setter::accept),
          (property, buffer) -> new LongPropertyData(property, buffer.readVarLong())),
    SHORT(Short.TYPE, (short) 0, (getter, setter) -> SyncableShort.create(() -> (short) getter.get(), setter::accept),
          (property, buffer) -> new ShortPropertyData(property, buffer.readShort())),
    ITEM_STACK(ItemStack.class, ItemStack.EMPTY, (getter, setter) -> SyncableItemStack.create(() -> (ItemStack) getter.get(), setter::accept),
          (property, buffer) -> new ItemStackPropertyData(property, buffer.readItemStack())),
    FLUID_STACK(FluidStack.class, FluidStack.EMPTY, (getter, setter) -> SyncableFluidStack.create(() -> (FluidStack) getter.get(), setter::accept),
          (property, buffer) -> new FluidStackPropertyData(property, buffer.readFluidStack())),
    GAS_STACK(GasStack.class, GasStack.EMPTY, (getter, setter) -> SyncableGasStack.create(() -> (GasStack) getter.get(), setter::accept),
          (property, buffer) -> new GasStackPropertyData(property, ChemicalUtils.readGasStack(buffer))),
    INFUSION_STACK(InfusionStack.class, InfusionStack.EMPTY, (getter, setter) -> SyncableInfusionStack.create(() -> (InfusionStack) getter.get(), setter::accept),
          (property, buffer) -> new InfusionStackPropertyData(property, ChemicalUtils.readInfusionStack(buffer))),
    PIGMENT_STACK(PigmentStack.class, PigmentStack.EMPTY, (getter, setter) -> SyncablePigmentStack.create(() -> (PigmentStack) getter.get(), setter::accept),
          (property, buffer) -> new PigmentStackPropertyData(property, ChemicalUtils.readPigmentStack(buffer))),
    SLURRY_STACK(SlurryStack.class, SlurryStack.EMPTY, (getter, setter) -> SyncableSlurryStack.create(() -> (SlurryStack) getter.get(), setter::accept),
          (property, buffer) -> new SlurryStackPropertyData(property, ChemicalUtils.readSlurryStack(buffer))),
    FREQUENCY(Frequency.class, null, (getter, setter) -> SyncableFrequency.create(() -> (Frequency) getter.get(), setter::accept),
          FrequencyPropertyData::readFrequency),
    LIST(ArrayList.class, Collections.emptyList(), (getter, setter) -> null /* not handled */, ListPropertyData::readList),
    BLOCK_POS(BlockPos.class, null, (getter, setter) -> SyncableBlockPos.create(() -> (BlockPos) getter.get(), setter::accept),
          (property, buffer) -> new BlockPosPropertyData(property, buffer.readBoolean() ? buffer.readBlockPos() : null)),
    FLOATING_LONG(FloatingLong.class, FloatingLong.ZERO, (getter, setter) -> SyncableFloatingLong.create(() -> (FloatingLong) getter.get(), setter::accept),
          (property, buffer) -> new FloatingLongPropertyData(property, FloatingLong.readFromBuffer(buffer)));

    private final Class<?> type;
    private final Object defaultValue;
    private final BiFunction<Supplier<Object>, Consumer<Object>, ISyncableData> creatorFunction;
    private final BiFunction<Short, PacketBuffer, PropertyData> dataCreatorFunction;

    private static final PropertyType[] VALUES = values();

    PropertyType(Class<?> type, Object defaultValue, BiFunction<Supplier<Object>, Consumer<Object>, ISyncableData> creatorFunction,
          BiFunction<Short, PacketBuffer, PropertyData> dataCreatorFunction) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.creatorFunction = creatorFunction;
        this.dataCreatorFunction = dataCreatorFunction;
    }

    public <T> T getDefault() {
        return (T) defaultValue;
    }

    public static PropertyType getFromType(Class<?> type) {
        for (PropertyType propertyType : VALUES) {
            if (type == propertyType.type) {
                return propertyType;
            }
        }

        return null;
    }

    public PropertyData createData(short property, PacketBuffer buffer) {
        return dataCreatorFunction.apply(property, buffer);
    }

    public ISyncableData create(Supplier<Object> supplier, Consumer<Object> consumer) {
        return creatorFunction.apply(supplier, consumer);
    }
}