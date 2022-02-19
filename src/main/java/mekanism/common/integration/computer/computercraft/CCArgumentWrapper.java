package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.integration.computer.ComputerArgumentHandler;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.text.InputValidator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.VisibleForTesting;

public class CCArgumentWrapper extends ComputerArgumentHandler<LuaException, MethodResult> {

    private static final double MAX_FLOATING_LONG_AS_DOUBLE = Double.parseDouble(FloatingLong.MAX_VALUE.toString());
    //Note: We use an int and a string as the key types for type hints so that in deserialization we know that a map
    // that has both of these can't be an actual map representing an array, list, or compound as those all have keys
    // of a single type (arrays and lists have doubles, and compounds have strings). We also need to specifically
    // declare the value key as a double as that is what CC will give us back
    @VisibleForTesting
    static final String TYPE_HINT_KEY = "typeHint";
    @VisibleForTesting
    static final Double TYPE_HINT_VALUE_KEY = 0D;

    private final IArguments arguments;

    CCArgumentWrapper(IArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public int getCount() {
        return arguments.count();
    }

    @Override
    public LuaException error(String messageFormat, Object... args) {
        return new LuaException(formatError(messageFormat, args));
    }

    @Nullable
    @Override
    public Object getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    public Object sanitizeArgument(Class<?> expectedType, Class<?> argumentType, Object argument) {
        //Types that ComputerCraft may send us: Boolean, Double, String, Map
        if (argumentType == Double.class) {
            //Note: We only check it against Double and not the primitive type as ComputerCraft only passes us a Double object
            double d = (double) argument;
            if (Double.isFinite(d)) {
                //Only allow finite doubles
                //Note: Our implementations of these clamp the values to within the range of the various types
                // if the value is out of range, then we just don't accept it as a value
                if (expectedType == Byte.TYPE || expectedType == Byte.class) {
                    if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                        return (byte) d;
                    }
                } else if (expectedType == Short.TYPE || expectedType == Short.class) {
                    if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                        return (short) d;
                    }
                } else if (expectedType == Integer.TYPE || expectedType == Integer.class) {
                    if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                        return (int) d;
                    }
                } else if (expectedType == Long.TYPE || expectedType == Long.class) {
                    if (d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        return (long) d;
                    }
                } else if (expectedType == Float.TYPE || expectedType == Float.class) {
                    if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Note: MIN_VALUE on float is the smallest positive number
                        return (float) d;
                    }
                } else if (expectedType == FloatingLong.class) {
                    if (d >= 0 && d <= MAX_FLOATING_LONG_AS_DOUBLE) {
                        //Only allow positive numbers as floating longs and ones that will fit within the bounds
                        // even though basically all should fit within the bounds
                        return FloatingLong.createConst(d);
                    }
                } else if (expectedType.isEnum()) {
                    if (d >= 0) {
                        Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) expectedType).getEnumConstants();
                        if (d < enumConstants.length) {
                            return enumConstants[(int) d];
                        }
                    }
                }
            }
        } else if (argumentType == String.class) {
            if (expectedType == char[].class) {
                //Allow casting string to character arrays
                return ((String) argument).toCharArray();
            } else if (expectedType == Character.TYPE || expectedType == Character.class) {
                String string = (String) argument;
                if (string.length() == 1) {
                    //If our input string is of length one allow "casting" it to a char/Character
                    return string.charAt(0);
                }
            } else if (expectedType == ResourceLocation.class) {
                ResourceLocation rl = ResourceLocation.tryParse((String) argument);
                if (rl != null) {
                    return rl;
                }
            } else if (expectedType.isEnum()) {
                Object sanitized = sanitizeStringToEnum((Class<? extends Enum<?>>) expectedType, (String) argument);
                if (sanitized != null) {
                    return sanitized;
                }
            } else if (Item.class.isAssignableFrom(expectedType)) {
                Item item = tryCreateItem(argument);
                if (expectedType.isInstance(item)) {
                    return item;
                }
            }
        } else if (argument instanceof Map) {
            if (IFilter.class.isAssignableFrom(expectedType)) {
                //Note: instanceof has slightly better performance than if we would instead fo Map.class.isAssignableFrom(argumentType)
                Object sanitized = convertMapToFilter(expectedType, (Map<?, ?>) argument);
                if (sanitized != null) {
                    return sanitized;
                }
            }
        }
        //Handle nbt types as a fallback check
        if (INBT.class.isAssignableFrom(expectedType)) {
            Object sanitized = sanitizeNBT(expectedType, argumentType, argument);
            if (sanitized != null) {
                return sanitized;
            }
        }
        return super.sanitizeArgument(expectedType, argumentType, argument);
    }

    @Override
    public MethodResult noResult() {
        return MethodResult.of();
    }

    @Override
    public MethodResult wrapResult(Object result) {
        return MethodResult.of(wrapReturnType(result));
    }

    private static Object wrapReturnType(Object result) {
        if (result == null || result instanceof Number || result instanceof Boolean || result instanceof String) {
            //Short circuit if it doesn't need wrapping
            return result;
        } else if (result instanceof ResourceLocation || result instanceof UUID) {
            return result.toString();
        } else if (result instanceof ForgeRegistryEntry<?>) {
            return getName((ForgeRegistryEntry<?>) result);
        } else if (result instanceof ChemicalStack<?>) {
            ChemicalStack<?> stack = (ChemicalStack<?>) result;
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getType()));
            wrapped.put("amount", stack.getAmount());
            return wrapped;
        } else if (result instanceof FluidStack) {
            FluidStack stack = (FluidStack) result;
            return wrapStack(stack.getFluid(), "amount", stack.getAmount(), stack.getTag());
        } else if (result instanceof ItemStack) {
            ItemStack stack = (ItemStack) result;
            return wrapStack(stack.getItem(), "count", stack.getCount(), stack.getTag());
        } else if (result instanceof INBT) {
            Object wrapped = wrapNBT((INBT) result);
            if (wrapped != null) {
                return wrapped;
            }
        } else if (result instanceof Vector3i) {
            //BlockPos is covered by this case
            Vector3i pos = (Vector3i) result;
            Map<String, Object> wrapped = new HashMap<>(3);
            wrapped.put("x", pos.getX());
            wrapped.put("y", pos.getY());
            wrapped.put("z", pos.getZ());
            return wrapped;
        } else if (result instanceof Coord4D) {
            //BlockPos is covered by this case
            Coord4D coord = (Coord4D) result;
            Map<String, Object> wrapped = new HashMap<>(4);
            wrapped.put("x", coord.getX());
            wrapped.put("y", coord.getY());
            wrapped.put("z", coord.getZ());
            wrapped.put("dimension", wrapReturnType(coord.dimension.location()));
            return wrapped;
        } else if (result instanceof Frequency) {
            FrequencyIdentity identity = ((Frequency) result).getIdentity();
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("key", wrapReturnType(identity.getKey()));
            wrapped.put("public", identity.isPublic());
            return wrapped;
        } else if (result instanceof Enum) {
            return ((Enum<?>) result).name();
        } else if (result instanceof IFilter) {
            Map<String, Object> wrapped = new HashMap<>();
            wrapped.put("type", wrapReturnType(((IFilter<?>) result).getFilterType()));
            if (result instanceof IItemStackFilter) {
                ItemStack stack = ((IItemStackFilter<?>) result).getItemStack();
                wrapped.put("item", wrapReturnType(stack.getItem()));
                if (!stack.isEmpty()) {
                    CompoundNBT tag = stack.getTag();
                    if (tag != null && !tag.isEmpty()) {
                        wrapped.put("itemNBT", wrapNBT(tag));
                    }
                }
            } else if (result instanceof IMaterialFilter) {
                wrapped.put("materialItem", wrapReturnType(((IMaterialFilter<?>) result).getMaterialItem().getItem()));
            } else if (result instanceof IModIDFilter) {
                wrapped.put("modId", ((IModIDFilter<?>) result).getModID());
            } else if (result instanceof ITagFilter) {
                wrapped.put("tag", ((ITagFilter<?>) result).getTagName());
            }
            if (result instanceof MinerFilter) {
                MinerFilter<?> minerFilter = (MinerFilter<?>) result;
                wrapped.put("requiresReplacement", minerFilter.requiresReplacement);
                wrapped.put("replaceTarget", wrapReturnType(minerFilter.replaceTarget));
            } else if (result instanceof SorterFilter) {
                SorterFilter<?> sorterFilter = (SorterFilter<?>) result;
                wrapped.put("allowDefault", sorterFilter.allowDefault);
                wrapped.put("color", wrapReturnType(sorterFilter.color));
                wrapped.put("size", sorterFilter.sizeMode);
                wrapped.put("min", sorterFilter.min);
                wrapped.put("max", sorterFilter.max);
                if (sorterFilter instanceof SorterItemStackFilter) {
                    SorterItemStackFilter filter = (SorterItemStackFilter) sorterFilter;
                    wrapped.put("fuzzy", filter.fuzzyMode);
                }
            } else if (result instanceof QIOFilter) {
                QIOFilter<?> qioFilter = (QIOFilter<?>) result;
                if (qioFilter instanceof QIOItemStackFilter) {
                    QIOItemStackFilter filter = (QIOItemStackFilter) qioFilter;
                    wrapped.put("fuzzy", filter.fuzzyMode);
                }
            } else if (result instanceof OredictionificatorFilter) {
                OredictionificatorFilter<?, ?, ?> filter = (OredictionificatorFilter<?, ?, ?>) result;
                wrapped.put("target", filter.getFilterText());
                wrapped.put("selected", wrapReturnType(filter.getResultElement()));
            }
            return wrapped;
        } else if (result instanceof Map) {
            return ((Map<?, ?>) result).entrySet().stream().collect(Collectors.toMap(entry -> wrapReturnType(entry.getKey()), entry -> wrapReturnType(entry.getValue()),
                  (a, b) -> b));
        } else if (result instanceof Collection) {
            //Note: We support any "collection" as it doesn't really matter if it is a set vs a list because
            // on ComputerCraft's end it will just be converted from a collection to a table, and be iterated
            // so there is no real difference at that point about the type it is
            return ((Collection<?>) result).stream().map(CCArgumentWrapper::wrapReturnType).collect(Collectors.toList());
        } else if (result instanceof Object[]) {
            //Note: This doesn't handle/deal with primitive arrays
            return Arrays.stream((Object[]) result).map(CCArgumentWrapper::wrapReturnType).toArray();
        }
        return result;
    }

    private static Map<String, Object> wrapStack(ForgeRegistryEntry<?> entry, String sizeKey, int amount, @Nullable CompoundNBT tag) {
        boolean hasTag = tag != null && !tag.isEmpty() && amount > 0;
        Map<String, Object> wrapped = new HashMap<>(hasTag ? 3 : 2);
        wrapped.put("name", getName(entry));
        wrapped.put(sizeKey, amount);
        if (hasTag) {
            wrapped.put("nbt", wrapNBT(tag));
        }
        return wrapped;
    }

    @Nullable
    private static Object wrapNBT(@Nullable INBT nbt) {
        if (nbt == null) {
            return null;
        }
        switch (nbt.getId()) {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
            case Constants.NBT.TAG_ANY_NUMERIC:
                return ((NumberNBT) nbt).getAsNumber();
            case Constants.NBT.TAG_STRING:
            case Constants.NBT.TAG_END://Tag End is highly unlikely to ever be used outside of networking but handle it anyway
                return nbt.getAsString();
            case Constants.NBT.TAG_BYTE_ARRAY:
            case Constants.NBT.TAG_INT_ARRAY:
            case Constants.NBT.TAG_LONG_ARRAY:
            case Constants.NBT.TAG_LIST:
                CollectionNBT<?> collectionNBT = (CollectionNBT<?>) nbt;
                int size = collectionNBT.size();
                Map<Integer, Object> wrappedCollection = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    wrappedCollection.put(i, wrapNBT(collectionNBT.get(i)));
                }
                return wrappedCollection;
            case Constants.NBT.TAG_COMPOUND:
                CompoundNBT compound = (CompoundNBT) nbt;
                Map<String, Object> wrappedCompound = new HashMap<>(compound.size());
                for (String key : compound.getAllKeys()) {
                    Object value = wrapNBT(compound.get(key));
                    if (value != null) {
                        wrappedCompound.put(key, value);
                    }
                }
                return wrappedCompound;
        }
        return null;
    }

    private static String getName(ForgeRegistryEntry<?> entry) {
        ResourceLocation registryName = entry.getRegistryName();
        return registryName == null ? null : registryName.toString();
    }

    @Nullable
    private static <ENUM extends Enum<?>> ENUM sanitizeStringToEnum(Class<? extends ENUM> expectedType, String argument) {
        if (!argument.isEmpty()) {
            ENUM[] enumConstants = expectedType.getEnumConstants();
            for (ENUM enumConstant : enumConstants) {
                if (argument.equalsIgnoreCase(enumConstant.name())) {
                    //Note: Strictly speaking enums can have the same name but different casing,
                    // but as all the enums we are using are all capital, this should not matter
                    return enumConstant;
                }
            }
        }
        return null;
    }

    private static ItemStack tryCreateFilterItem(@Nullable Object rawName, @Nullable Object rawNBT) {
        Item item = tryCreateItem(rawName);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (rawNBT != null) {
            INBT nbt = sanitizeNBT(CompoundNBT.class, rawNBT.getClass(), rawNBT);
            if (!(nbt instanceof CompoundNBT)) {
                //Failed to deserialize properly, there is an issue with the passed in lua side
                return ItemStack.EMPTY;
            }
            stack.setTag((CompoundNBT) nbt);
        }
        return stack;
    }

    private static Item tryCreateItem(@Nullable Object rawName) {
        if (rawName instanceof String) {
            ResourceLocation itemName = ResourceLocation.tryParse((String) rawName);
            if (itemName != null) {
                Item item = ForgeRegistries.ITEMS.getValue(itemName);
                if (item != null) {
                    return item;
                }
            }
        }
        return Items.AIR;
    }

    @Nullable
    private static String tryGetFilterTag(@Nullable Object rawTag) {
        if (rawTag instanceof String) {
            String tag = (String) rawTag;
            if (!tag.isEmpty()) {
                tag = tag.toLowerCase(Locale.ROOT);
                if (InputValidator.test(tag, InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS))) {
                    return tag;
                }
            }
        }
        return null;
    }

    @Nullable
    private static String tryGetFilterModId(@Nullable Object rawModId) {
        if (rawModId instanceof String) {
            String modId = (String) rawModId;
            if (!modId.isEmpty()) {
                modId = modId.toLowerCase(Locale.ROOT);
                if (InputValidator.test(modId, InputValidator.RL_NAMESPACE.or(InputValidator.WILDCARD_CHARS))) {
                    return modId;
                }
            }
        }
        return null;
    }

    private static boolean getBooleanFromRaw(@Nullable Object raw) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        return false;
    }

    private static int getIntFromRaw(@Nullable Object raw) {
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        return 0;
    }

    @Nullable
    private static Object convertMapToFilter(Class<?> expectedType, Map<?, ?> map) {
        //We may want to try improving this at some point, or somehow making it slightly less hardcoded
        // but for now this will have to do
        Object type = map.get("type");
        if (type instanceof String) {
            //Handle filters as arguments, this may not be the best implementation, but it will do for now
            FilterType filterType = sanitizeStringToEnum(FilterType.class, (String) type);
            if (filterType != null) {
                IFilter<?> filter = BaseFilter.fromType(filterType);
                if (expectedType.isInstance(filter)) {
                    //Validate the filter is of the type we expect
                    if (filter instanceof IItemStackFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("item"), map.get("itemNBT"));
                        if (stack.isEmpty()) {
                            return null;
                        }
                        ((IItemStackFilter<?>) filter).setItemStack(stack);
                    } else if (filter instanceof IMaterialFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("materialItem"), null);
                        if (stack.isEmpty()) {
                            return null;
                        }
                        ((IMaterialFilter<?>) filter).setMaterialItem(stack);
                    } else if (filter instanceof IModIDFilter) {
                        String modId = tryGetFilterModId(map.get("modId"));
                        if (modId == null) {
                            return null;
                        }
                        ((IModIDFilter<?>) filter).setModID(modId);
                    } else if (filter instanceof ITagFilter) {
                        String tag = tryGetFilterTag(map.get("tag"));
                        if (tag == null) {
                            return null;
                        }
                        ((ITagFilter<?>) filter).setTagName(tag);
                    }
                    if (filter instanceof MinerFilter) {
                        MinerFilter<?> minerFilter = (MinerFilter<?>) filter;
                        minerFilter.requiresReplacement = getBooleanFromRaw(map.get("requiresReplacement"));
                        minerFilter.replaceTarget = tryCreateItem(map.get("replaceTarget"));
                    } else if (filter instanceof SorterFilter) {
                        SorterFilter<?> sorterFilter = (SorterFilter<?>) filter;
                        sorterFilter.allowDefault = getBooleanFromRaw(map.get("allowDefault"));
                        Object rawColor = map.get("color");
                        if (rawColor instanceof String) {
                            sorterFilter.color = sanitizeStringToEnum(EnumColor.class, (String) rawColor);
                        }
                        sorterFilter.sizeMode = getBooleanFromRaw(map.get("size"));
                        sorterFilter.min = getIntFromRaw(map.get("min"));
                        sorterFilter.max = getIntFromRaw(map.get("max"));
                        if (sorterFilter.min < 0 || sorterFilter.max < 0 || sorterFilter.min > sorterFilter.max || sorterFilter.max > 64) {
                            return null;
                        }
                        if (sorterFilter instanceof SorterItemStackFilter) {
                            SorterItemStackFilter sorterItemFilter = (SorterItemStackFilter) sorterFilter;
                            sorterItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
                        }
                    } else if (filter instanceof QIOFilter) {
                        QIOFilter<?> qioFilter = (QIOFilter<?>) filter;
                        if (qioFilter instanceof QIOItemStackFilter) {
                            QIOItemStackFilter qioItemFilter = (QIOItemStackFilter) qioFilter;
                            qioItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
                        }
                    } else if (filter instanceof OredictionificatorFilter) {
                        OredictionificatorFilter<?, ?, ?> oredictionificatorFilter = (OredictionificatorFilter<?, ?, ?>) filter;
                        Object rawTag = map.get("target");
                        if (!(rawTag instanceof String)) {
                            return null;
                        }
                        String tag = (String) rawTag;
                        if (tag.isEmpty()) {
                            return null;
                        }
                        ResourceLocation rl = ResourceLocation.tryParse(tag);
                        if (rl == null || !TileEntityOredictionificator.isValidTarget(rl)) {
                            return null;
                        }
                        oredictionificatorFilter.setFilter(rl);
                        if (oredictionificatorFilter instanceof OredictionificatorItemFilter) {
                            Item item = tryCreateItem(map.get("selected"));
                            if (item != Items.AIR) {
                                ((OredictionificatorItemFilter) oredictionificatorFilter).setSelectedOutput(item);
                            }
                        }
                    }
                    return filter;
                }
            }
        }
        return null;
    }

    @Nullable
    private static INBT sanitizeNBT(Class<?> expectedType, Class<?> argumentType, Object argument) {
        if (argumentType == Boolean.class) {
            if (expectedType == ByteNBT.class || expectedType == NumberNBT.class || expectedType == INBT.class) {
                return ByteNBT.valueOf((boolean) argument);
            }
        } else if (argumentType == Double.class) {
            double d = (double) argument;
            if (Double.isFinite(d)) {
                if (expectedType == ByteNBT.class) {
                    if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                        return ByteNBT.valueOf((byte) d);
                    }
                } else if (expectedType == ShortNBT.class) {
                    if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                        return ShortNBT.valueOf((short) d);
                    }
                } else if (expectedType == IntNBT.class) {
                    if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                        return IntNBT.valueOf((int) d);
                    }
                } else if (expectedType == LongNBT.class) {
                    if (d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        return LongNBT.valueOf((long) d);
                    }
                } else if (expectedType == FloatNBT.class) {
                    if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Note: MIN_VALUE on float is the smallest positive number
                        return FloatNBT.valueOf((float) d);
                    }
                } else if (expectedType == DoubleNBT.class) {
                    return DoubleNBT.valueOf(d);
                } else if (expectedType == NumberNBT.class || expectedType == INBT.class) {
                    //Handle generic number or nbt specification
                    if (d == Math.floor(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        //If the number is an integer type and not out of range of longs
                        if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                            return ByteNBT.valueOf((byte) d);
                        } else if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                            return ShortNBT.valueOf((short) d);
                        } else if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                            return IntNBT.valueOf((int) d);
                        }
                        return LongNBT.valueOf((long) d);
                    } else if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Otherwise, if it is in the range of a float give float nbt
                        return FloatNBT.valueOf((float) d);
                    }
                    return DoubleNBT.valueOf(d);
                }
            }
        } else if (argumentType == String.class) {
            if (expectedType == StringNBT.class || expectedType == INBT.class) {
                return StringNBT.valueOf((String) argument);
            } else if (expectedType == EndNBT.class && argument.equals("END")) {
                //Unlikely to ever be the expected case but handle it anyway as it is easy.
                return EndNBT.INSTANCE;
            }
        } else if (argument instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) argument;
            if (map.size() == 2) {
                //If the size of the map is two, check if it is a hint for the type of NBT
                //TODO: We may want to document the existence of this typeHint system somewhere
                Object value = map.get(CCArgumentWrapper.TYPE_HINT_VALUE_KEY);
                Object typeHintRaw = map.get(CCArgumentWrapper.TYPE_HINT_KEY);
                if (value != null && typeHintRaw instanceof Double) {
                    double hint = (double) typeHintRaw;
                    if (Double.isFinite(hint) && (hint == Constants.NBT.TAG_ANY_NUMERIC || (hint >= Constants.NBT.TAG_END && hint <= Constants.NBT.TAG_LONG_ARRAY))) {
                        Class<? extends INBT> hinted = getTypeFromHint((int) hint);
                        if (expectedType == hinted || expectedType == INBT.class) {
                            //Hint is same as the type we were expecting, or we are expecting any type of NBT
                            return sanitizeNBT(hinted, value.getClass(), value);
                        } else if (expectedType == NumberNBT.class) {
                            if (hinted == ByteNBT.class || hinted == ShortNBT.class || hinted == IntNBT.class ||
                                hinted == LongNBT.class || hinted == FloatNBT.class || hinted == DoubleNBT.class) {
                                //If it is a specific type of number use that
                                return sanitizeNBT(hinted, value.getClass(), value);
                            }
                        } else if (expectedType == CollectionNBT.class) {
                            if (hinted == ByteArrayNBT.class || hinted == IntArrayNBT.class || hinted == LongArrayNBT.class || hinted == ListNBT.class) {
                                //If it is a specific type of collection use that
                                return sanitizeNBT(hinted, value.getClass(), value);
                            }
                        }
                        //Otherwise, if the expected type doesn't match the type we have a hint for fail
                        return null;
                    }
                }
            }
            Set<? extends Map.Entry<?, ?>> entries = map.entrySet();
            if (expectedType == ByteArrayNBT.class) {
                byte[] bytes = new byte[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE,
                      (key, value) -> bytes[key] = value.byteValue(), Constants.NBT.TAG_BYTE))) {
                    return new ByteArrayNBT(bytes);
                }
            } else if (expectedType == IntArrayNBT.class) {
                int[] ints = new int[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE,
                      (key, value) -> ints[key] = value.intValue(), Constants.NBT.TAG_INT))) {
                    return new IntArrayNBT(ints);
                }
            } else if (expectedType == LongArrayNBT.class) {
                long[] longs = new long[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Long.MIN_VALUE && value <= Long.MAX_VALUE,
                      (key, value) -> longs[key] = value.longValue(), Constants.NBT.TAG_LONG))) {
                    return new LongArrayNBT(longs);
                }
            } else if (expectedType == ListNBT.class) {
                return sanitizeNBTList(entries);
            } else if (expectedType == CollectionNBT.class) {
                return sanitizeNBTCollection(entries);
            } else if (expectedType == CompoundNBT.class) {
                return sanitizeNBTCompound(entries);
            } else if (expectedType == INBT.class) {
                Optional<? extends Map.Entry<?, ?>> element = entries.stream().findAny();
                if (element.isPresent() && element.get().getKey() instanceof Double) {
                    //If the map is not empty and the key is a double parse it as a collection
                    return sanitizeNBTCollection(entries);
                }
                // Otherwise, if it is empty or the key is not a double handle it as an NBT Compound
                return sanitizeNBTCompound(entries);
            }
        }
        return null;
    }

    private static Class<? extends INBT> getTypeFromHint(int hint) {
        switch (hint) {
            case Constants.NBT.TAG_BYTE:
                return ByteNBT.class;
            case Constants.NBT.TAG_SHORT:
                return ShortNBT.class;
            case Constants.NBT.TAG_INT:
                return IntNBT.class;
            case Constants.NBT.TAG_LONG:
                return LongNBT.class;
            case Constants.NBT.TAG_FLOAT:
                return FloatNBT.class;
            case Constants.NBT.TAG_DOUBLE:
                return DoubleNBT.class;
            case Constants.NBT.TAG_ANY_NUMERIC:
                return NumberNBT.class;
            case Constants.NBT.TAG_STRING:
                return StringNBT.class;
            case Constants.NBT.TAG_BYTE_ARRAY:
                return ByteArrayNBT.class;
            case Constants.NBT.TAG_INT_ARRAY:
                return IntArrayNBT.class;
            case Constants.NBT.TAG_LONG_ARRAY:
                return LongArrayNBT.class;
            case Constants.NBT.TAG_LIST:
                return ListNBT.class;
            case Constants.NBT.TAG_COMPOUND:
                return CompoundNBT.class;
            case Constants.NBT.TAG_END:
                return EndNBT.class;
        }
        return INBT.class;
    }

    @Nullable
    private static CompoundNBT sanitizeNBTCompound(Set<? extends Map.Entry<?, ?>> entries) {
        CompoundNBT nbt = new CompoundNBT();
        for (Map.Entry<?, ?> entry : entries) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                //If the key isn't a String: fail
                return null;
            }
            Object value = entry.getValue();
            INBT nbtValue = sanitizeNBT(INBT.class, value.getClass(), value);
            if (nbtValue == null || nbt.put((String) key, nbtValue) != null) {
                //If the value is not an NBT value, or we already have an entry in our compound for that key: fail
                return null;
            }
        }
        return nbt;
    }

    @Nullable
    private static CollectionNBT<?> sanitizeNBTCollection(Set<? extends Map.Entry<?, ?>> entries) {
        ListNBT nbtList = sanitizeNBTList(entries);
        if (nbtList != null) {
            //If we have a list, if it is either entirely bytes, ints, or longs then convert it to the corresponding array type
            if (nbtList.getElementType() == Constants.NBT.TAG_BYTE) {
                return new ByteArrayNBT(nbtList.stream().map(e -> ((ByteNBT) e).getAsByte()).collect(Collectors.toList()));
            } else if (nbtList.getElementType() == Constants.NBT.TAG_INT) {
                return new IntArrayNBT(nbtList.stream().mapToInt(e -> ((IntNBT) e).getAsInt()).toArray());
            } else if (nbtList.getElementType() == Constants.NBT.TAG_LONG) {
                return new LongArrayNBT(nbtList.stream().mapToLong(e -> ((LongNBT) e).getAsLong()).toArray());
            }
            return nbtList;
        }
        return null;
    }

    @Nullable
    private static ListNBT sanitizeNBTList(Set<? extends Map.Entry<?, ?>> entries) {
        ValidateAndConsumeListElement val = new ValidateAndConsumeListElement(entries.size());
        return sanitizeNBTCollection(entries, val) ? val.toList() : null;
    }

    private static boolean sanitizeNBTCollection(Set<? extends Map.Entry<?, ?>> entries, BiFunction<Integer, Object, Boolean> validateAndConsumeValue) {
        boolean[] valuesSet = new boolean[entries.size()];
        for (Map.Entry<?, ?> entry : entries) {
            Object key = entry.getKey();
            if (!(key instanceof Double)) {
                //If the key isn't a number: fail
                return false;
            }
            double k = (double) key;
            if (!Double.isFinite(k) || k < 0 || k >= valuesSet.length) {
                //If the key isn't a finite positive integer: fail
                return false;
            }
            int intKey = (int) k;
            if (valuesSet[intKey] || !validateAndConsumeValue.apply(intKey, entry.getValue())) {
                //If we have already set this value or the value is not valid: fail
                return false;
            }
            valuesSet[intKey] = true;
        }
        //Note: We don't need to validate that all our values have been set as if there is an issue
        // we exit early, and we can only get to here if each of our entries have been iterated
        return true;
    }

    private static class ArrayElementValidator implements BiFunction<Integer, Object, Boolean> {

        private final DoublePredicate rangeValidator;
        private final BiConsumer<Integer, Double> consumeValue;
        private final int expectedType;

        public ArrayElementValidator(DoublePredicate rangeValidator, BiConsumer<Integer, Double> consumeValue, int expectedType) {
            this.rangeValidator = rangeValidator;
            this.consumeValue = consumeValue;
            this.expectedType = expectedType;
        }

        @Override
        public Boolean apply(Integer key, Object value) {
            if (value instanceof Double) {
                double v = (double) value;
                if (Double.isFinite(v) && rangeValidator.test(v)) {
                    consumeValue.accept(key, v);
                    return true;
                }
            } else if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                if (map.size() == 2) {
                    //If the size of the map is two, check if it is a hint for the type of NBT. This is a massively reduced
                    // hint matching check as we can make a lot of assumptions about how this method is used and not have
                    // to handle cases like generic number or raw INBT type as the current matching type
                    Object hintValue = map.get(CCArgumentWrapper.TYPE_HINT_VALUE_KEY);
                    Object typeHintRaw = map.get(CCArgumentWrapper.TYPE_HINT_KEY);
                    if (hintValue != null && typeHintRaw instanceof Double && (double) typeHintRaw == expectedType) {
                        return apply(key, hintValue);
                    }
                }
            }
            return false;
        }
    }

    private static class ValidateAndConsumeListElement implements BiFunction<Integer, Object, Boolean> {

        private final INBT[] elements;
        private Class<? extends INBT> desiredClass = INBT.class;
        private Class<? extends INBT> searchClass = INBT.class;

        public ValidateAndConsumeListElement(int size) {
            this.elements = new INBT[size];
        }

        @Override
        public Boolean apply(Integer key, Object value) {
            INBT element = sanitizeNBT(searchClass, value.getClass(), value);
            if (element == null) {
                return false;
            }
            Class<? extends INBT> elementClass = element.getClass();
            if (elementClass == EndNBT.class) {
                //ListNBT does not support tag end
                return false;
            }
            //Validate the type matches as ListNBT requires all elements to be of the same type
            if (desiredClass == INBT.class) {
                desiredClass = elementClass;
                if (element instanceof NumberNBT) {
                    searchClass = NumberNBT.class;
                } else if (elementClass == ListNBT.class) {
                    //Otherwise, only check ListNBTs. We can get away with this because our generic CollectionNBT/INBT
                    // handling first tries to get it into an array before a List so we are only here if it isn't a supported array type
                    if (((ListNBT) element).getElementType() == Constants.NBT.TAG_SHORT) {
                        //If the inner type is a short, check collections
                        // Note: It is not possible for it to be an *empty* list here as our base check is for INBT
                        // which would cause us to end up with an empty CompoundNBT instead
                        searchClass = CollectionNBT.class;
                    } else
                        searchClass = elementClass;
                } else if (element instanceof CollectionNBT) {
                    //Arrays
                    searchClass = CollectionNBT.class;
                } else if (!(element instanceof CompoundNBT) || !((CompoundNBT) element).isEmpty()) {
                    //If it is not a CompoundNBT, or it is not an empty CompoundNBT mark it as the type we search for
                    // otherwise if it is an empty CompoundNBT allow it to search INBT to try and get other things like arrays/lists
                    searchClass = elementClass;
                }
            } else if (desiredClass != elementClass) {
                if (element instanceof NumberNBT) {
                    if (validUpcast(desiredClass, elementClass)) {
                        desiredClass = elementClass;
                    } else if (!validUpcast(elementClass, desiredClass)) {
                        //This is unlikely to ever end up going in here just due to them both being numbers
                        // and us having checked if we can upcast from either type to the other one, but if
                        // we ever enter this invalid state return false to signify it
                        return false;
                    }
                } else if (elementClass == ByteArrayNBT.class) {
                    //Allow casting bytes up to int arrays, long arrays, or short lists
                    // To allow for easier reading the below if statement is can be written as the negation of this:
                    // desiredClass == IntArrayNBT.class || desiredClass == LongArrayNBT.class || (desiredClass == ListNBT.class && searchClass == CollectionNBT.class)
                    if (desiredClass != IntArrayNBT.class && desiredClass != LongArrayNBT.class && (desiredClass != ListNBT.class || searchClass != CollectionNBT.class)) {
                        //If it is not one of them, check if we are expecting an empty compound (which is indistinguishable from an empty array)
                        if (desiredClass == CompoundNBT.class && searchClass == INBT.class) {
                            // and if we are, allow it but transition the target over to byte arrays
                            desiredClass = ByteArrayNBT.class;
                            searchClass = CollectionNBT.class;
                        } else {
                            return false;
                        }
                    }
                } else if (elementClass == IntArrayNBT.class) {
                    if (desiredClass == ByteArrayNBT.class || (desiredClass == ListNBT.class && searchClass == CollectionNBT.class)) {
                        //If we think we only have byte arrays or a list of shorts but find and int array, promote to an int array
                        desiredClass = IntArrayNBT.class;
                    } else if (desiredClass != LongArrayNBT.class) {
                        if (desiredClass == CompoundNBT.class && searchClass == INBT.class) {
                            //Don't actually return false and instead adjust the type information to matching an int array
                            // if we only have empty NBT compounds so far as they are indistinguishable from arrays.
                            desiredClass = IntArrayNBT.class;
                            searchClass = CollectionNBT.class;
                        } else {
                            return false;
                        }
                    }
                } else if (elementClass == LongArrayNBT.class) {
                    if (desiredClass == ByteArrayNBT.class || desiredClass == IntArrayNBT.class || (desiredClass == ListNBT.class && searchClass == CollectionNBT.class)) {
                        //If we have a byte array, int array, or short list, promote our desired type to a long array
                        desiredClass = LongArrayNBT.class;
                    } else if (desiredClass == CompoundNBT.class && searchClass == INBT.class) {
                        //If we instead an empty compound (which is indistinguishable from an empty array)
                        // convert it over to a long array
                        desiredClass = LongArrayNBT.class;
                        searchClass = CollectionNBT.class;
                    } else {
                        return false;
                    }
                } else if (elementClass == ListNBT.class) {
                    byte listType = ((ListNBT) element).getElementType();
                    if (desiredClass == ByteArrayNBT.class) {
                        //If we found a short list, and we want byte arrays, switch to a short list
                        // otherwise if we just found a general list we need to transition it all over to a normal list
                        // if we found an empty list (as we are searching for collections), just do nothing as it is compatible with a byte array
                        if (listType != Constants.NBT.TAG_END) {
                            desiredClass = ListNBT.class;
                            if (listType != Constants.NBT.TAG_SHORT) {
                                searchClass = ListNBT.class;
                            }
                        }
                    } else if (desiredClass == IntArrayNBT.class || desiredClass == LongArrayNBT.class) {
                        //We can upcast the short into an int or long array and convert empty lists into the corresponding array types
                        if (listType != Constants.NBT.TAG_END && listType != Constants.NBT.TAG_SHORT) {
                            // but if our list is not a short list then we need to convert the existing arrays we have to lists
                            desiredClass = ListNBT.class;
                            searchClass = ListNBT.class;
                        }
                    } else if (desiredClass == CompoundNBT.class && searchClass == INBT.class) {
                        //If we instead an empty compound (which is indistinguishable from an empty list)
                        // convert it over to a list
                        desiredClass = ListNBT.class;
                        if (listType == Constants.NBT.TAG_SHORT) {
                            //If we are a short list search for any collection type
                            searchClass = CollectionNBT.class;
                        } else {
                            // otherwise, only search for lists
                            searchClass = ListNBT.class;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (elementClass == ListNBT.class) {
                //If we only have short lists
                if (searchClass == CollectionNBT.class) {
                    byte listType = ((ListNBT) element).getElementType();
                    // and our current element is not also a list of shorts or an empty list
                    if (listType != Constants.NBT.TAG_END && listType != Constants.NBT.TAG_SHORT) {
                        // then we need to convert our search class to only being nbt lists
                        searchClass = ListNBT.class;
                    }
                }
            } else if (elementClass == CompoundNBT.class) {
                //If they are both CompoundNBTs and our element isn't empty, and we have only had empty
                // compounds so far, then mark that we are a CompoundNBT and not just empty/guessing
                if (!((CompoundNBT) element).isEmpty() && searchClass == INBT.class) {
                    searchClass = elementClass;
                }
            }
            elements[key] = element;
            return true;
        }

        public ListNBT toList() {
            ListNBT listNBT = new ListNBT();
            //Note: We don't need to special case ByteNBT as there isn't any types that can upcast into it
            if (desiredClass == ShortNBT.class) {
                Arrays.stream(elements).map(element -> element instanceof ShortNBT ? element : ShortNBT.valueOf(((NumberNBT) element).getAsShort()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == IntNBT.class) {
                Arrays.stream(elements).map(element -> element instanceof IntNBT ? element : IntNBT.valueOf(((NumberNBT) element).getAsInt()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == LongNBT.class) {
                Arrays.stream(elements).map(element -> element instanceof LongNBT ? element : LongNBT.valueOf(((NumberNBT) element).getAsLong()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == FloatNBT.class) {
                Arrays.stream(elements).map(element -> element instanceof FloatNBT ? element : FloatNBT.valueOf(((NumberNBT) element).getAsFloat()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == DoubleNBT.class) {
                Arrays.stream(elements).map(element -> element instanceof DoubleNBT ? element : DoubleNBT.valueOf(((NumberNBT) element).getAsDouble()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == ByteArrayNBT.class) {
                for (INBT element : elements) {
                    if (element instanceof ByteArrayNBT) {
                        listNBT.add(element);
                    } else if (element instanceof CompoundNBT || element instanceof ListNBT) {//Empty compound or empty list
                        listNBT.add(new ByteArrayNBT(new byte[0]));
                    }
                }
            } else if (desiredClass == IntArrayNBT.class) {
                for (INBT element : elements) {
                    if (element instanceof IntArrayNBT) {
                        listNBT.add(element);
                    } else if (element instanceof ByteArrayNBT) {
                        byte[] values = ((ByteArrayNBT) element).getAsByteArray();
                        listNBT.add(new IntArrayNBT(IntStream.range(0, values.length).map(i -> values[i]).toArray()));
                    } else if (element instanceof CompoundNBT) {//Empty compound
                        listNBT.add(new IntArrayNBT(new int[0]));
                    } else if (element instanceof ListNBT) {//List of shorts or empty list
                        listNBT.add(new IntArrayNBT(((ListNBT) element).stream().mapToInt(nbt -> ((ShortNBT) nbt).getAsInt()).toArray()));
                    }
                }
            } else if (desiredClass == LongArrayNBT.class) {
                for (INBT element : elements) {
                    if (element instanceof LongArrayNBT) {
                        listNBT.add(element);
                    } else if (element instanceof IntArrayNBT) {
                        listNBT.add(new LongArrayNBT(Arrays.stream(((IntArrayNBT) element).getAsIntArray()).asLongStream().toArray()));
                    } else if (element instanceof ByteArrayNBT) {
                        byte[] values = ((ByteArrayNBT) element).getAsByteArray();
                        listNBT.add(new LongArrayNBT(IntStream.range(0, values.length).map(i -> values[i]).asLongStream().toArray()));
                    } else if (element instanceof CompoundNBT) {//Empty compound
                        listNBT.add(new LongArrayNBT(new long[0]));
                    } else if (element instanceof ListNBT) {//List of shorts or empty list
                        listNBT.add(new LongArrayNBT(((ListNBT) element).stream().mapToLong(nbt -> ((ShortNBT) nbt).getAsLong()).toArray()));
                    }
                }
            } else if (desiredClass == ListNBT.class) {
                for (INBT element : elements) {
                    if (element instanceof ListNBT) {
                        listNBT.add(element);
                    } else if (element instanceof CompoundNBT) {//Empty compound
                        listNBT.add(new ListNBT());
                    } else if (element instanceof CollectionNBT) {//Byte int or long array
                        ListNBT byteList = new ListNBT();
                        byteList.addAll((CollectionNBT<?>) element);
                        listNBT.add(byteList);
                    }
                }
            } else {
                Collections.addAll(listNBT, elements);
            }
            return listNBT;
        }

        private static boolean validUpcast(Class<? extends INBT> argument, Class<? extends INBT> target) {
            if (argument == ByteNBT.class) {
                return target == ShortNBT.class || target == IntNBT.class || target == LongNBT.class || target == FloatNBT.class || target == DoubleNBT.class;
            } else if (argument == ShortNBT.class) {
                return target == IntNBT.class || target == LongNBT.class || target == FloatNBT.class || target == DoubleNBT.class;
            } else if (argument == IntNBT.class) {
                return target == LongNBT.class || target == FloatNBT.class || target == DoubleNBT.class;
            } else if (argument == LongNBT.class) {
                return target == FloatNBT.class || target == DoubleNBT.class;
            } else if (argument == FloatNBT.class) {
                return target == DoubleNBT.class;
            }
            return false;
        }
    }
}