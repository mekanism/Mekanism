package mekanism.common.integration.computer;

import mekanism.api.math.FloatingLong;
import mekanism.common.content.filter.IFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Thiakil on 15/07/2023.
 */
public interface FancyComputerHelper {
    Enum<?> getEnum(int param, Class<? extends Enum<?>> enumClazz);

    boolean getBool(int param);

    byte getByte(int param);

    short getShort(int param);

    int getInt(int param);

    long getLong(int param);

    char getChar(int param);

    float getFloat(int param);

    double getDouble(int param);

    FloatingLong getFloatingLong(int param);

    ResourceLocation getResourceLocation(int param);

    String getString(int param);

    Item getItem(int param);

    IFilter<?> getFilter(int param, Class<? extends IFilter<?>> expectedType);

    Object voidResult();

    Object result(Object rawResult);

    default Object result(@Nullable FloatingLong result) {
        return result(result == null ? 0 : result.doubleValue());
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T getEnum(FancyComputerHelper helper, int param, Class<T> enumClass) {
        return (T) helper.getEnum(param, enumClass);
    }

    @SuppressWarnings("unchecked")
    static <T extends IFilter<T>> T getFilter(FancyComputerHelper helper, int param, Class<T> expectedType) {
        return (T) helper.getFilter(param, expectedType);
    }
}
