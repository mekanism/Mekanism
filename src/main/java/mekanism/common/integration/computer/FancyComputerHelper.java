package mekanism.common.integration.computer;

import mekanism.api.math.FloatingLong;
import mekanism.common.content.filter.IFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public interface FancyComputerHelper {
    <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException;

    boolean getBool(int param) throws ComputerException;

    byte getByte(int param) throws ComputerException;

    short getShort(int param) throws ComputerException;

    int getInt(int param) throws ComputerException;

    long getLong(int param) throws ComputerException;

    char getChar(int param) throws ComputerException;

    float getFloat(int param) throws ComputerException;

    double getDouble(int param) throws ComputerException;

    FloatingLong getFloatingLong(int param) throws ComputerException;

    String getString(int param) throws ComputerException;

    <FILTER extends IFilter<FILTER>> FILTER getFilter(int param, Class<FILTER> expectedType) throws ComputerException;

    default ResourceLocation getResourceLocation(int param) throws ComputerException {
        return ResourceLocation.tryParse(getString(param));
    }

    default Item getItem(int param) throws ComputerException {
        ResourceLocation itemName = getResourceLocation(param);
        if (itemName != null) {
            Item item = ForgeRegistries.ITEMS.getValue(itemName);
            if (item != null) {
                return item;
            }
        }
        return Items.AIR;
    }

    Object voidResult();

    Object result(Object rawResult);

    default Object result(@Nullable FloatingLong result) {
        return result(result == null ? 0 : result.doubleValue());
    }
}
