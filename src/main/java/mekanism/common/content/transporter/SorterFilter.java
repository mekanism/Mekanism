package mekanism.common.content.transporter;

import com.mojang.datafixers.Products.P6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public abstract class SorterFilter<FILTER extends SorterFilter<FILTER>> extends BaseFilter<FILTER> {

    public static final int MAX_LENGTH = 48;

    protected static <FILTER extends SorterFilter<FILTER>> P6<Mu<FILTER>, Boolean, Boolean, Boolean, Integer, Integer, Optional<EnumColor>> baseSorterCodec(Instance<FILTER> instance) {
        return baseCodec(instance)
              .and(Codec.BOOL.optionalFieldOf(SerializationConstants.ALLOW_DEFAULT, false).forGetter(filter -> filter.allowDefault))
              .and(Codec.BOOL.optionalFieldOf(SerializationConstants.SIZE, false).forGetter(filter -> filter.sizeMode))
              .and(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(SerializationConstants.MIN, 0).forGetter(filter -> filter.min))
              .and(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(SerializationConstants.MAX, 0).forGetter(filter -> filter.max))
              .and(EnumColor.CODEC.optionalFieldOf(SerializationConstants.COLOR).forGetter(filter -> Optional.ofNullable(filter.color)))
              ;
    }

    protected static <FILTER extends SorterFilter<FILTER>> StreamCodec<ByteBuf, FILTER> baseSorterStreamCodec(Supplier<FILTER> constructor) {
        return StreamCodec.composite(
              baseStreamCodec(constructor), Function.identity(),
              ByteBufCodecs.BOOL, filter -> filter.allowDefault,
              ByteBufCodecs.BOOL, filter -> filter.sizeMode,
              ByteBufCodecs.VAR_INT, filter -> filter.min,
              ByteBufCodecs.VAR_INT, filter -> filter.max,
              EnumColor.OPTIONAL_STREAM_CODEC, filter -> Optional.ofNullable(filter.color),
              (filter, allowDefault, sizeMode, min, max, color) -> {
                  filter.allowDefault = allowDefault;
                  filter.color = color.orElse(null);
                  filter.sizeMode = sizeMode;
                  filter.min = min;
                  filter.max = max;
                  return filter;
              }
        );
    }

    @Nullable
    @SyntheticComputerMethod(getter = "getColor", setter = "setColor", threadSafeGetter = true, threadSafeSetter = true)
    public EnumColor color;
    @SyntheticComputerMethod(getter = "getAllowDefault", setter = "setAllowDefault", threadSafeGetter = true, threadSafeSetter = true)
    public boolean allowDefault;
    @SyntheticComputerMethod(getter = "getSizeMode", setter = "setSizeMode", threadSafeSetter = true, threadSafeGetter = true)
    public boolean sizeMode;
    @SyntheticComputerMethod(getter = "getMin", threadSafeGetter = true)
    public int min;
    @SyntheticComputerMethod(getter = "getMax", threadSafeGetter = true)
    public int max;

    protected SorterFilter() {
    }

    protected SorterFilter(boolean enabled, boolean allowDefault, boolean sizeMode, int min, int max, @Nullable EnumColor color) {
        super(enabled);
        this.allowDefault = allowDefault;
        this.sizeMode = sizeMode;
        this.min = min;
        this.max = max;
        this.color = color;
    }

    protected SorterFilter(FILTER filter) {
        super(filter);
        allowDefault = filter.allowDefault;
        color = filter.color;
        sizeMode = filter.sizeMode;
        min = filter.min;
        max = filter.max;
    }

    public abstract Finder getFinder();

    public TransitRequest mapInventory(IItemHandler itemHandler, boolean singleItem) {
        if (sizeMode && !singleItem) {
            return TransitRequest.definedItem(itemHandler, min, max, getFinder());
        }
        return TransitRequest.definedItem(itemHandler, singleItem ? 1 : Item.ABSOLUTE_MAX_STACK_SIZE, getFinder());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(color);
        result = 31 * result + Boolean.hashCode(allowDefault);
        result = 31 * result + Boolean.hashCode(sizeMode);
        result = 31 * result + min;
        result = 31 * result + max;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        SorterFilter<?> other = (SorterFilter<?>) o;
        return allowDefault == other.allowDefault && sizeMode == other.sizeMode && min == other.min && max == other.max && color == other.color;
    }

    @ComputerMethod(threadSafe = true)
    void setMinMax(int min, int max) throws ComputerException {
        if (min < 0 || max < 0 || min > max || max > Item.ABSOLUTE_MAX_STACK_SIZE) {
            throw new ComputerException("Invalid or min/max: 0 <= min <= max <= " + Item.ABSOLUTE_MAX_STACK_SIZE);
        }
        this.min = min;
        this.max = max;
    }

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}