package mekanism.common.integration.computer;

import mekanism.api.Coord4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.util.RegistryUtils;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseComputerHelper {
    public <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException {
        return SpecialConverters.sanitizeStringToEnum(enumClazz, getString(param));
    }

    public abstract  boolean getBool(int param) throws ComputerException;

    public abstract  byte getByte(int param) throws ComputerException;

    public abstract  short getShort(int param) throws ComputerException;

    public abstract  int getInt(int param) throws ComputerException;

    public abstract  long getLong(int param) throws ComputerException;

    public abstract  char getChar(int param) throws ComputerException;

    public abstract  float getFloat(int param) throws ComputerException;

    public abstract  double getDouble(int param) throws ComputerException;

    public  FloatingLong getFloatingLong(int param) throws ComputerException {
        double finiteDouble = getDouble(param);
        if (finiteDouble < 0) {
            return FloatingLong.ZERO;
        }
        return FloatingLong.createConst(finiteDouble);
    }

    public abstract  String getString(int param) throws ComputerException;

    public abstract Map<?,?> getMap(int param) throws ComputerException;

    public <FILTER extends IFilter<FILTER>> FILTER getFilter(int param, Class<FILTER> expectedType) throws ComputerException {
        return SpecialConverters.convertMapToFilter(expectedType,getMap(param));
    }

    public ResourceLocation getResourceLocation(int param) throws ComputerException {
        return ResourceLocation.tryParse(getString(param));
    }

    public Item getItem(int param) throws ComputerException {
        ResourceLocation itemName = getResourceLocation(param);
        if (itemName != null) {
            Item item = ForgeRegistries.ITEMS.getValue(itemName);
            if (item != null) {
                return item;
            }
        }
        return Items.AIR;
    }

    public Object voidResult() {
        return null;
    }

    public Object convert(@Nullable FloatingLong result) {
        return result == null ? 0 : result.doubleValue();
    }

    public Object convert(int i) {
        return i;
    }

    public Object convert(long i) {
        return i;
    }

    public Object convert(double d) {
        return d;
    }

    public Object convert(String s) {
        return s;
    }

    public Object convert(boolean b) {
        return b;
    }

    public <T> Object convert(Collection<T> list, Function<T, Object> converter) {
        if (list == null) return Collections.emptyList();
        List<Object> converted = new ArrayList<>(list.size());
        for (T el : list) {
            converted.add(converter.apply(el));
        }
        return converted;
    }

    public Object convert(ResourceLocation rl) {
        return rl.toString();
    }

    public Object convert(UUID uuid) {
        return uuid.toString();
    }

    public Object convert(ChemicalStack<?> stack) {
        Map<String, Object> wrapped = new HashMap<>(2);
        wrapped.put("name", stack.getTypeRegistryName().toString());
        wrapped.put("amount", stack.getAmount());
        return wrapped;
    }

    public Object convert(FluidStack stack) {
        return SpecialConverters.wrapStack(RegistryUtils.getName(stack.getFluid()), "amount", stack.getAmount(), stack.getTag());
    }

    public Object convert(ItemStack stack) {
        return SpecialConverters.wrapStack(RegistryUtils.getName(stack.getItem()), "count", stack.getCount(), stack.getTag());
    }

    public Object convert(BlockState state) {
        Map<String, Object> wrapped = new HashMap<>(2);
        ResourceLocation name = RegistryUtils.getName(state.getBlock());
        if (name != null) {
            wrapped.put("block", name.toString());
        }
        Map<String, Object> stateData = new HashMap<>();
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            Property<?> property = entry.getKey();
            Object value = entry.getValue();
            if (!(property instanceof IntegerProperty) && !(property instanceof BooleanProperty)) {
                value = Util.getPropertyName(property, value);
            }
            stateData.put(property.getName(), value);
        }
        if (!stateData.isEmpty()) {
            wrapped.put("state", stateData);
        }
        return wrapped;
    }

    public Object convert(Vec3i pos) {
        //BlockPos is covered by this case
        Map<String, Object> wrapped = new HashMap<>(3);
        wrapped.put("x", pos.getX());
        wrapped.put("y", pos.getY());
        wrapped.put("z", pos.getZ());
        return wrapped;
    }

    public Object convert(Coord4D coord) {
        Map<String, Object> wrapped = new HashMap<>(4);
        wrapped.put("x", coord.getX());
        wrapped.put("y", coord.getY());
        wrapped.put("z", coord.getZ());
        wrapped.put("dimension", convert(coord.dimension.location()));
        return wrapped;
    }

    public Object convert(Frequency frequency) {
        Frequency.FrequencyIdentity identity = frequency.getIdentity();
        Map<String, Object> wrapped = new HashMap<>(2);
        wrapped.put("key", identity.key().toString());//todo something better here
        wrapped.put("public", identity.isPublic());
        return wrapped;
    }

    public Object convert(Enum<?> res) {
        return res.name();
    }

    public Object convert(IFilter<?> result) {
        Map<String, Object> wrapped = new HashMap<>();
        wrapped.put("type", convert(result.getFilterType()));
        wrapped.put("enabled", result.isEnabled());
        if (result instanceof IItemStackFilter<?> itemFilter) {
            ItemStack stack = itemFilter.getItemStack();
            wrapped.put("item", convert(stack.getItem()));
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.getTag();
                if (tag != null && !tag.isEmpty()) {
                    wrapped.put("itemNBT", SpecialConverters.wrapNBT(tag));
                }
            }
        } else if (result instanceof IModIDFilter<?> modIDFilter) {
            wrapped.put("modId", modIDFilter.getModID());
        } else if (result instanceof ITagFilter<?> tagFilter) {
            wrapped.put("tag", tagFilter.getTagName());
        }
        if (result instanceof MinerFilter<?> minerFilter) {
            wrapped.put("requiresReplacement", minerFilter.requiresReplacement);
            wrapped.put("replaceTarget", convert(minerFilter.replaceTarget));
        } else if (result instanceof SorterFilter<?> sorterFilter) {
            wrapped.put("allowDefault", sorterFilter.allowDefault);
            wrapped.put("color", convert(sorterFilter.color));
            wrapped.put("size", sorterFilter.sizeMode);
            wrapped.put("min", sorterFilter.min);
            wrapped.put("max", sorterFilter.max);
            if (sorterFilter instanceof SorterItemStackFilter filter) {
                wrapped.put("fuzzy", filter.fuzzyMode);
            }
        } else if (result instanceof QIOFilter<?> qioFilter) {
            if (qioFilter instanceof QIOItemStackFilter filter) {
                wrapped.put("fuzzy", filter.fuzzyMode);
            }
        } else if (result instanceof OredictionificatorFilter<?, ?, ?> filter) {
            wrapped.put("target", filter.getFilterText());
            Object resultElement = filter.getResultElement();
            wrapped.put("selected", convert(resultElement instanceof Item ? (Item)resultElement : null));//todo only Item seems to be used?
        }
        return wrapped;
    }

    public <KEY, VALUE> Object convert(Map<KEY, VALUE> res, Function<KEY, Object> keyConverter, Function<VALUE, Object> valueConverter) {
        return res.entrySet().stream().collect(Collectors.toMap(entry -> keyConverter.apply(entry.getKey()), entry -> valueConverter.apply(entry.getValue()), (a, b) -> b));
    }

    public Object convert(Item item) {
        return RegistryUtils.getName(item);
    }

    public Object convert(Convertable<?> convertable) {
        return convertable.convert(this);
    }

    /**
     * Convert a type to the converted version (what is exposed to the computer)
     *
     * @param clazz the unconverted type
     * @return the converted type, or clazz if no conversion needed
     */
    public static Class<?> convertType(Class<?> clazz) {
        if (clazz == UUID.class || clazz == ResourceLocation.class || clazz == Item.class || Enum.class.isAssignableFrom(clazz)) {
            return String.class;
        }
        if (clazz == Frequency.class || clazz == Coord4D.class || clazz == Vec3i.class || clazz == FluidStack.class || clazz == ItemStack.class || clazz == BlockState.class) {
            return Map.class;
        }
        if (ChemicalStack.class.isAssignableFrom(clazz) || IFilter.class.isAssignableFrom(clazz)) {
            return Map.class;
        }
        if (clazz == Convertable.class) {
            return Map.class;//technically can be anything, but so far only map used
        }
        return clazz;
    }
}
