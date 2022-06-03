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
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        //noinspection unchecked
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
            } else if (expectedType == FloatingLong.class) {
                try {
                    return FloatingLong.parseFloatingLong((String) argument);
                } catch (NumberFormatException ignored) {
                }
            } else if (expectedType.isEnum()) {
                //noinspection unchecked
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
        } else if (argument instanceof Map<?, ?> arg) {
            if (IFilter.class.isAssignableFrom(expectedType)) {
                //Note: instanceof has slightly better performance than if we would instead of Map.class.isAssignableFrom(argumentType)
                Object sanitized = convertMapToFilter(expectedType, arg);
                if (sanitized != null) {
                    return sanitized;
                }
            }
        }
        //Handle nbt types as a fallback check
        if (Tag.class.isAssignableFrom(expectedType)) {
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
        } else if (result instanceof ForgeRegistryEntry<?> registryEntry) {
            return getName(registryEntry);
        } else if (result instanceof ChemicalStack<?> stack) {
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getType()));
            wrapped.put("amount", stack.getAmount());
            return wrapped;
        } else if (result instanceof FluidStack stack) {
            return wrapStack(stack.getFluid(), "amount", stack.getAmount(), stack.getTag());
        } else if (result instanceof ItemStack stack) {
            return wrapStack(stack.getItem(), "count", stack.getCount(), stack.getTag());
        } else if (result instanceof Tag tag) {
            Object wrapped = wrapNBT(tag);
            if (wrapped != null) {
                return wrapped;
            }
        } else if (result instanceof Vec3i pos) {
            //BlockPos is covered by this case
            Map<String, Object> wrapped = new HashMap<>(3);
            wrapped.put("x", pos.getX());
            wrapped.put("y", pos.getY());
            wrapped.put("z", pos.getZ());
            return wrapped;
        } else if (result instanceof Coord4D coord) {
            //BlockPos is covered by this case
            Map<String, Object> wrapped = new HashMap<>(4);
            wrapped.put("x", coord.getX());
            wrapped.put("y", coord.getY());
            wrapped.put("z", coord.getZ());
            wrapped.put("dimension", wrapReturnType(coord.dimension.location()));
            return wrapped;
        } else if (result instanceof Frequency frequency) {
            FrequencyIdentity identity = frequency.getIdentity();
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("key", wrapReturnType(identity.key()));
            wrapped.put("public", identity.isPublic());
            return wrapped;
        } else if (result instanceof Enum<?> res) {
            return res.name();
        } else if (result instanceof IFilter res) {
            Map<String, Object> wrapped = new HashMap<>();
            wrapped.put("type", wrapReturnType(res.getFilterType()));
            if (result instanceof IItemStackFilter<?> itemFilter) {
                ItemStack stack = itemFilter.getItemStack();
                wrapped.put("item", wrapReturnType(stack.getItem()));
                if (!stack.isEmpty()) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && !tag.isEmpty()) {
                        wrapped.put("itemNBT", wrapNBT(tag));
                    }
                }
            } else if (result instanceof IMaterialFilter<?> materialFilter) {
                wrapped.put("materialItem", wrapReturnType(materialFilter.getMaterialItem().getItem()));
            } else if (result instanceof IModIDFilter<?> modIDFilter) {
                wrapped.put("modId", modIDFilter.getModID());
            } else if (result instanceof ITagFilter<?> tagFilter) {
                wrapped.put("tag", tagFilter.getTagName());
            }
            if (result instanceof MinerFilter<?> minerFilter) {
                wrapped.put("requiresReplacement", minerFilter.requiresReplacement);
                wrapped.put("replaceTarget", wrapReturnType(minerFilter.replaceTarget));
            } else if (result instanceof SorterFilter<?> sorterFilter) {
                wrapped.put("allowDefault", sorterFilter.allowDefault);
                wrapped.put("color", wrapReturnType(sorterFilter.color));
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
                wrapped.put("selected", wrapReturnType(filter.getResultElement()));
            }
            return wrapped;
        } else if (result instanceof Map<?, ?> res) {
            return res.entrySet().stream().collect(Collectors.toMap(entry -> wrapReturnType(entry.getKey()), entry -> wrapReturnType(entry.getValue()), (a, b) -> b));
        } else if (result instanceof Collection<?> res) {
            //Note: We support any "collection" as it doesn't really matter if it is a set vs a list because
            // on ComputerCraft's end it will just be converted from a collection to a table, and be iterated
            // so there is no real difference at that point about the type it is
            return res.stream().map(CCArgumentWrapper::wrapReturnType).toList();
        } else if (result instanceof Object[] res) {
            //Note: This doesn't handle/deal with primitive arrays
            return Arrays.stream(res).map(CCArgumentWrapper::wrapReturnType).toArray();
        }
        return result;
    }

    private static Map<String, Object> wrapStack(ForgeRegistryEntry<?> entry, String sizeKey, int amount, @Nullable CompoundTag tag) {
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
    private static Object wrapNBT(@Nullable Tag nbt) {
        if (nbt == null) {
            return null;
        }
        return switch (nbt.getId()) {
            case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG, Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> ((NumericTag) nbt).getAsNumber();
            //Tag End is highly unlikely to ever be used outside of networking but handle it anyway
            case Tag.TAG_STRING, Tag.TAG_END -> nbt.getAsString();
            case Tag.TAG_BYTE_ARRAY, Tag.TAG_INT_ARRAY, Tag.TAG_LONG_ARRAY, Tag.TAG_LIST -> {
                CollectionTag<?> collectionNBT = (CollectionTag<?>) nbt;
                int size = collectionNBT.size();
                Map<Integer, Object> wrappedCollection = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    wrappedCollection.put(i, wrapNBT(collectionNBT.get(i)));
                }
                yield wrappedCollection;
            }
            case Tag.TAG_COMPOUND -> {
                CompoundTag compound = (CompoundTag) nbt;
                Map<String, Object> wrappedCompound = new HashMap<>(compound.size());
                for (String key : compound.getAllKeys()) {
                    Object value = wrapNBT(compound.get(key));
                    if (value != null) {
                        wrappedCompound.put(key, value);
                    }
                }
                yield wrappedCompound;
            }
            default -> null;
        };
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
            Tag nbt = sanitizeNBT(CompoundTag.class, rawNBT.getClass(), rawNBT);
            if (!(nbt instanceof CompoundTag)) {
                //Failed to deserialize properly, there is an issue with the passed in lua side
                return ItemStack.EMPTY;
            }
            stack.setTag((CompoundTag) nbt);
        }
        return stack;
    }

    private static Item tryCreateItem(@Nullable Object rawName) {
        if (rawName instanceof String name) {
            ResourceLocation itemName = ResourceLocation.tryParse(name);
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
        if (rawTag instanceof String tag && !tag.isEmpty()) {
            tag = tag.toLowerCase(Locale.ROOT);
            if (InputValidator.test(tag, InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS))) {
                return tag;
            }
        }
        return null;
    }

    @Nullable
    private static String tryGetFilterModId(@Nullable Object rawModId) {
        if (rawModId instanceof String modId && !modId.isEmpty()) {
            modId = modId.toLowerCase(Locale.ROOT);
            if (InputValidator.test(modId, InputValidator.RL_NAMESPACE.or(InputValidator.WILDCARD_CHARS))) {
                return modId;
            }
        }
        return null;
    }

    private static boolean getBooleanFromRaw(@Nullable Object raw) {
        return raw instanceof Boolean bool ? bool : Boolean.valueOf(false);
    }

    private static int getIntFromRaw(@Nullable Object raw) {
        return raw instanceof Number number ? number.intValue() : 0;
    }

    @Nullable
    private static Object convertMapToFilter(Class<?> expectedType, Map<?, ?> map) {
        //We may want to try improving this at some point, or somehow making it slightly less hardcoded
        // but for now this will have to do
        Object type = map.get("type");
        if (type instanceof String string) {
            //Handle filters as arguments, this may not be the best implementation, but it will do for now
            FilterType filterType = sanitizeStringToEnum(FilterType.class, string);
            if (filterType != null) {
                IFilter<?> filter = BaseFilter.fromType(filterType);
                if (expectedType.isInstance(filter)) {
                    //Validate the filter is of the type we expect
                    if (filter instanceof IItemStackFilter<?> itemFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("item"), map.get("itemNBT"));
                        if (stack.isEmpty()) {
                            return null;
                        }
                        itemFilter.setItemStack(stack);
                    } else if (filter instanceof IMaterialFilter<?> materialFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("materialItem"), null);
                        if (stack.isEmpty()) {
                            return null;
                        }
                        materialFilter.setMaterialItem(stack);
                    } else if (filter instanceof IModIDFilter<?> modIDFilter) {
                        String modId = tryGetFilterModId(map.get("modId"));
                        if (modId == null) {
                            return null;
                        }
                        modIDFilter.setModID(modId);
                    } else if (filter instanceof ITagFilter<?> tagFilter) {
                        String tag = tryGetFilterTag(map.get("tag"));
                        if (tag == null) {
                            return null;
                        }
                        tagFilter.setTagName(tag);
                    }
                    if (filter instanceof MinerFilter<?> minerFilter) {
                        minerFilter.requiresReplacement = getBooleanFromRaw(map.get("requiresReplacement"));
                        minerFilter.replaceTarget = tryCreateItem(map.get("replaceTarget"));
                    } else if (filter instanceof SorterFilter<?> sorterFilter) {
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
                        if (sorterFilter instanceof SorterItemStackFilter sorterItemFilter) {
                            sorterItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
                        }
                    } else if (filter instanceof QIOFilter<?> qioFilter) {
                        if (qioFilter instanceof QIOItemStackFilter qioItemFilter) {
                            qioItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
                        }
                    } else if (filter instanceof OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) {
                        Object rawTag = map.get("target");
                        if (!(rawTag instanceof String tag)) {
                            return null;
                        }
                        if (tag.isEmpty()) {
                            return null;
                        }
                        ResourceLocation rl = ResourceLocation.tryParse(tag);
                        if (rl == null || !TileEntityOredictionificator.isValidTarget(rl)) {
                            return null;
                        }
                        oredictionificatorFilter.setFilter(rl);
                        if (oredictionificatorFilter instanceof OredictionificatorItemFilter itemFilter) {
                            Item item = tryCreateItem(map.get("selected"));
                            if (item != Items.AIR) {
                                itemFilter.setSelectedOutput(item);
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
    private static Tag sanitizeNBT(Class<?> expectedType, Class<?> argumentType, Object argument) {
        if (argumentType == Boolean.class) {
            if (expectedType == ByteTag.class || expectedType == NumericTag.class || expectedType == Tag.class) {
                return ByteTag.valueOf((boolean) argument);
            }
        } else if (argumentType == Double.class) {
            double d = (double) argument;
            if (Double.isFinite(d)) {
                if (expectedType == ByteTag.class) {
                    if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                        return ByteTag.valueOf((byte) d);
                    }
                } else if (expectedType == ShortTag.class) {
                    if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                        return ShortTag.valueOf((short) d);
                    }
                } else if (expectedType == IntTag.class) {
                    if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                        return IntTag.valueOf((int) d);
                    }
                } else if (expectedType == LongTag.class) {
                    if (d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        return LongTag.valueOf((long) d);
                    }
                } else if (expectedType == FloatTag.class) {
                    if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Note: MIN_VALUE on float is the smallest positive number
                        return FloatTag.valueOf((float) d);
                    }
                } else if (expectedType == DoubleTag.class) {
                    return DoubleTag.valueOf(d);
                } else if (expectedType == NumericTag.class || expectedType == Tag.class) {
                    //Handle generic number or nbt specification
                    if (d == Math.floor(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        //If the number is an integer type and not out of range of longs
                        if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                            return ByteTag.valueOf((byte) d);
                        } else if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                            return ShortTag.valueOf((short) d);
                        } else if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                            return IntTag.valueOf((int) d);
                        }
                        return LongTag.valueOf((long) d);
                    } else if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Otherwise, if it is in the range of a float give float nbt
                        return FloatTag.valueOf((float) d);
                    }
                    return DoubleTag.valueOf(d);
                }
            }
        } else if (argumentType == String.class) {
            if (expectedType == StringTag.class || expectedType == Tag.class) {
                return StringTag.valueOf((String) argument);
            } else if (expectedType == EndTag.class && argument.equals("END")) {
                //Unlikely to ever be the expected case but handle it anyway as it is easy.
                return EndTag.INSTANCE;
            }
        } else if (argument instanceof Map<?, ?> map) {
            if (map.size() == 2) {
                //If the size of the map is two, check if it is a hint for the type of NBT
                //TODO: We may want to document the existence of this typeHint system somewhere
                Object value = map.get(CCArgumentWrapper.TYPE_HINT_VALUE_KEY);
                Object typeHintRaw = map.get(CCArgumentWrapper.TYPE_HINT_KEY);
                if (value != null && typeHintRaw instanceof Double) {
                    double hint = (double) typeHintRaw;
                    if (Double.isFinite(hint) && (hint == Tag.TAG_ANY_NUMERIC || (hint >= Tag.TAG_END && hint <= Tag.TAG_LONG_ARRAY))) {
                        Class<? extends Tag> hinted = getTypeFromHint((int) hint);
                        if (expectedType == hinted || expectedType == Tag.class) {
                            //Hint is same as the type we were expecting, or we are expecting any type of NBT
                            return sanitizeNBT(hinted, value.getClass(), value);
                        } else if (expectedType == NumericTag.class) {
                            if (hinted == ByteTag.class || hinted == ShortTag.class || hinted == IntTag.class ||
                                hinted == LongTag.class || hinted == FloatTag.class || hinted == DoubleTag.class) {
                                //If it is a specific type of number use that
                                return sanitizeNBT(hinted, value.getClass(), value);
                            }
                        } else if (expectedType == CollectionTag.class) {
                            if (hinted == ByteArrayTag.class || hinted == IntArrayTag.class || hinted == LongArrayTag.class || hinted == ListTag.class) {
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
            if (expectedType == ByteArrayTag.class) {
                byte[] bytes = new byte[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE,
                      (key, value) -> bytes[key] = value.byteValue(), Tag.TAG_BYTE))) {
                    return new ByteArrayTag(bytes);
                }
            } else if (expectedType == IntArrayTag.class) {
                int[] ints = new int[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE,
                      (key, value) -> ints[key] = value.intValue(), Tag.TAG_INT))) {
                    return new IntArrayTag(ints);
                }
            } else if (expectedType == LongArrayTag.class) {
                long[] longs = new long[entries.size()];
                if (sanitizeNBTCollection(entries, new ArrayElementValidator(value -> value >= Long.MIN_VALUE && value <= Long.MAX_VALUE,
                      (key, value) -> longs[key] = value.longValue(), Tag.TAG_LONG))) {
                    return new LongArrayTag(longs);
                }
            } else if (expectedType == ListTag.class) {
                return sanitizeNBTList(entries);
            } else if (expectedType == CollectionTag.class) {
                return sanitizeNBTCollection(entries);
            } else if (expectedType == CompoundTag.class) {
                return sanitizeNBTCompound(entries);
            } else if (expectedType == Tag.class) {
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

    private static Class<? extends Tag> getTypeFromHint(int hint) {
        return switch (hint) {
            case Tag.TAG_BYTE -> ByteTag.class;
            case Tag.TAG_SHORT -> ShortTag.class;
            case Tag.TAG_INT -> IntTag.class;
            case Tag.TAG_LONG -> LongTag.class;
            case Tag.TAG_FLOAT -> FloatTag.class;
            case Tag.TAG_DOUBLE -> DoubleTag.class;
            case Tag.TAG_ANY_NUMERIC -> NumericTag.class;
            case Tag.TAG_STRING -> StringTag.class;
            case Tag.TAG_BYTE_ARRAY -> ByteArrayTag.class;
            case Tag.TAG_INT_ARRAY -> IntArrayTag.class;
            case Tag.TAG_LONG_ARRAY -> LongArrayTag.class;
            case Tag.TAG_LIST -> ListTag.class;
            case Tag.TAG_COMPOUND -> CompoundTag.class;
            case Tag.TAG_END -> EndTag.class;
            default -> Tag.class;
        };
    }

    @Nullable
    private static CompoundTag sanitizeNBTCompound(Set<? extends Map.Entry<?, ?>> entries) {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<?, ?> entry : entries) {
            Object key = entry.getKey();
            if (!(key instanceof String string)) {
                //If the key isn't a String: fail
                return null;
            }
            Object value = entry.getValue();
            Tag nbtValue = sanitizeNBT(Tag.class, value.getClass(), value);
            if (nbtValue == null || nbt.put(string, nbtValue) != null) {
                //If the value is not an NBT value, or we already have an entry in our compound for that key: fail
                return null;
            }
        }
        return nbt;
    }

    @Nullable
    private static CollectionTag<?> sanitizeNBTCollection(Set<? extends Map.Entry<?, ?>> entries) {
        ListTag nbtList = sanitizeNBTList(entries);
        if (nbtList != null) {
            //If we have a list, if it is either entirely bytes, ints, or longs then convert it to the corresponding array type
            if (nbtList.getElementType() == Tag.TAG_BYTE) {
                return new ByteArrayTag(nbtList.stream().map(e -> ((ByteTag) e).getAsByte()).toList());
            } else if (nbtList.getElementType() == Tag.TAG_INT) {
                return new IntArrayTag(nbtList.stream().mapToInt(e -> ((IntTag) e).getAsInt()).toArray());
            } else if (nbtList.getElementType() == Tag.TAG_LONG) {
                return new LongArrayTag(nbtList.stream().mapToLong(e -> ((LongTag) e).getAsLong()).toArray());
            }
            return nbtList;
        }
        return null;
    }

    @Nullable
    private static ListTag sanitizeNBTList(Set<? extends Map.Entry<?, ?>> entries) {
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

    private record ArrayElementValidator(DoublePredicate rangeValidator, BiConsumer<Integer, Double> consumeValue, int expectedType) implements
          BiFunction<Integer, Object, Boolean> {

        @Override
        public Boolean apply(Integer key, Object value) {
            if (value instanceof Double) {
                double v = (double) value;
                if (Double.isFinite(v) && rangeValidator.test(v)) {
                    consumeValue.accept(key, v);
                    return true;
                }
            } else if (value instanceof Map<?, ?> map) {
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

        private final Tag[] elements;
        private Class<? extends Tag> desiredClass = Tag.class;
        private Class<? extends Tag> searchClass = Tag.class;

        public ValidateAndConsumeListElement(int size) {
            this.elements = new Tag[size];
        }

        @Override
        public Boolean apply(Integer key, Object value) {
            Tag element = sanitizeNBT(searchClass, value.getClass(), value);
            if (element == null) {
                return false;
            }
            Class<? extends Tag> elementClass = element.getClass();
            if (elementClass == EndTag.class) {
                //ListNBT does not support tag end
                return false;
            }
            //Validate the type matches as ListNBT requires all elements to be of the same type
            if (desiredClass == Tag.class) {
                desiredClass = elementClass;
                if (element instanceof NumericTag) {
                    searchClass = NumericTag.class;
                } else if (elementClass == ListTag.class) {
                    //Otherwise, only check ListNBTs. We can get away with this because our generic CollectionNBT/INBT
                    // handling first tries to get it into an array before a List so we are only here if it isn't a supported array type
                    if (((ListTag) element).getElementType() == Tag.TAG_SHORT) {
                        //If the inner type is a short, check collections
                        // Note: It is not possible for it to be an *empty* list here as our base check is for INBT
                        // which would cause us to end up with an empty CompoundNBT instead
                        searchClass = CollectionTag.class;
                    } else {
                        searchClass = elementClass;
                    }
                } else if (element instanceof CollectionTag) {
                    //Arrays
                    searchClass = CollectionTag.class;
                } else if (!(element instanceof CompoundTag tag) || !tag.isEmpty()) {
                    //If it is not a CompoundNBT, or it is not an empty CompoundNBT mark it as the type we search for
                    // otherwise if it is an empty CompoundNBT allow it to search INBT to try and get other things like arrays/lists
                    searchClass = elementClass;
                }
            } else if (desiredClass != elementClass) {
                if (element instanceof NumericTag) {
                    if (validUpcast(desiredClass, elementClass)) {
                        desiredClass = elementClass;
                    } else if (!validUpcast(elementClass, desiredClass)) {
                        //This is unlikely to ever end up going in here just due to them both being numbers
                        // and us having checked if we can upcast from either type to the other one, but if
                        // we ever enter this invalid state return false to signify it
                        return false;
                    }
                } else if (elementClass == ByteArrayTag.class) {
                    //Allow casting bytes up to int arrays, long arrays, or short lists
                    // To allow for easier reading the below if statement is can be written as the negation of this:
                    // desiredClass == IntArrayNBT.class || desiredClass == LongArrayNBT.class || (desiredClass == ListNBT.class && searchClass == CollectionNBT.class)
                    if (desiredClass != IntArrayTag.class && desiredClass != LongArrayTag.class && (desiredClass != ListTag.class || searchClass != CollectionTag.class)) {
                        //If it is not one of them, check if we are expecting an empty compound (which is indistinguishable from an empty array)
                        if (desiredClass == CompoundTag.class && searchClass == Tag.class) {
                            // and if we are, allow it but transition the target over to byte arrays
                            desiredClass = ByteArrayTag.class;
                            searchClass = CollectionTag.class;
                        } else {
                            return false;
                        }
                    }
                } else if (elementClass == IntArrayTag.class) {
                    if (desiredClass == ByteArrayTag.class || (desiredClass == ListTag.class && searchClass == CollectionTag.class)) {
                        //If we think we only have byte arrays or a list of shorts but find and int array, promote to an int array
                        desiredClass = IntArrayTag.class;
                    } else if (desiredClass != LongArrayTag.class) {
                        if (desiredClass == CompoundTag.class && searchClass == Tag.class) {
                            //Don't actually return false and instead adjust the type information to matching an int array
                            // if we only have empty NBT compounds so far as they are indistinguishable from arrays.
                            desiredClass = IntArrayTag.class;
                            searchClass = CollectionTag.class;
                        } else {
                            return false;
                        }
                    }
                } else if (elementClass == LongArrayTag.class) {
                    if (desiredClass == ByteArrayTag.class || desiredClass == IntArrayTag.class || (desiredClass == ListTag.class && searchClass == CollectionTag.class)) {
                        //If we have a byte array, int array, or short list, promote our desired type to a long array
                        desiredClass = LongArrayTag.class;
                    } else if (desiredClass == CompoundTag.class && searchClass == Tag.class) {
                        //If we instead an empty compound (which is indistinguishable from an empty array)
                        // convert it over to a long array
                        desiredClass = LongArrayTag.class;
                        searchClass = CollectionTag.class;
                    } else {
                        return false;
                    }
                } else if (elementClass == ListTag.class) {
                    byte listType = ((ListTag) element).getElementType();
                    if (desiredClass == ByteArrayTag.class) {
                        //If we found a short list, and we want byte arrays, switch to a short list
                        // otherwise if we just found a general list we need to transition it all over to a normal list
                        // if we found an empty list (as we are searching for collections), just do nothing as it is compatible with a byte array
                        if (listType != Tag.TAG_END) {
                            desiredClass = ListTag.class;
                            if (listType != Tag.TAG_SHORT) {
                                searchClass = ListTag.class;
                            }
                        }
                    } else if (desiredClass == IntArrayTag.class || desiredClass == LongArrayTag.class) {
                        //We can upcast the short into an int or long array and convert empty lists into the corresponding array types
                        if (listType != Tag.TAG_END && listType != Tag.TAG_SHORT) {
                            // but if our list is not a short list then we need to convert the existing arrays we have to lists
                            desiredClass = ListTag.class;
                            searchClass = ListTag.class;
                        }
                    } else if (desiredClass == CompoundTag.class && searchClass == Tag.class) {
                        //If we instead an empty compound (which is indistinguishable from an empty list)
                        // convert it over to a list
                        desiredClass = ListTag.class;
                        if (listType == Tag.TAG_SHORT) {
                            //If we are a short list search for any collection type
                            searchClass = CollectionTag.class;
                        } else {
                            // otherwise, only search for lists
                            searchClass = ListTag.class;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (elementClass == ListTag.class) {
                //If we only have short lists
                if (searchClass == CollectionTag.class) {
                    byte listType = ((ListTag) element).getElementType();
                    // and our current element is not also a list of shorts or an empty list
                    if (listType != Tag.TAG_END && listType != Tag.TAG_SHORT) {
                        // then we need to convert our search class to only being nbt lists
                        searchClass = ListTag.class;
                    }
                }
            } else if (elementClass == CompoundTag.class) {
                //If they are both CompoundNBTs and our element isn't empty, and we have only had empty
                // compounds so far, then mark that we are a CompoundNBT and not just empty/guessing
                if (!((CompoundTag) element).isEmpty() && searchClass == Tag.class) {
                    searchClass = elementClass;
                }
            }
            elements[key] = element;
            return true;
        }

        public ListTag toList() {
            ListTag listNBT = new ListTag();
            //Note: We don't need to special case ByteNBT as there isn't any types that can upcast into it
            if (desiredClass == ShortTag.class) {
                Arrays.stream(elements).map(element -> element instanceof ShortTag ? element : ShortTag.valueOf(((NumericTag) element).getAsShort()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == IntTag.class) {
                Arrays.stream(elements).map(element -> element instanceof IntTag ? element : IntTag.valueOf(((NumericTag) element).getAsInt()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == LongTag.class) {
                Arrays.stream(elements).map(element -> element instanceof LongTag ? element : LongTag.valueOf(((NumericTag) element).getAsLong()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == FloatTag.class) {
                Arrays.stream(elements).map(element -> element instanceof FloatTag ? element : FloatTag.valueOf(((NumericTag) element).getAsFloat()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == DoubleTag.class) {
                Arrays.stream(elements).map(element -> element instanceof DoubleTag ? element : DoubleTag.valueOf(((NumericTag) element).getAsDouble()))
                      .collect(Collectors.toCollection(() -> listNBT));
            } else if (desiredClass == ByteArrayTag.class) {
                for (Tag element : elements) {
                    if (element instanceof ByteArrayTag) {
                        listNBT.add(element);
                    } else if (element instanceof CompoundTag || element instanceof ListTag) {//Empty compound or empty list
                        listNBT.add(new ByteArrayTag(new byte[0]));
                    }
                }
            } else if (desiredClass == IntArrayTag.class) {
                for (Tag element : elements) {
                    if (element instanceof IntArrayTag) {
                        listNBT.add(element);
                    } else if (element instanceof ByteArrayTag tag) {
                        byte[] values = tag.getAsByteArray();
                        listNBT.add(new IntArrayTag(IntStream.range(0, values.length).map(i -> values[i]).toArray()));
                    } else if (element instanceof CompoundTag) {//Empty compound
                        listNBT.add(new IntArrayTag(new int[0]));
                    } else if (element instanceof ListTag tag) {//List of shorts or empty list
                        listNBT.add(new IntArrayTag(tag.stream().mapToInt(nbt -> ((ShortTag) nbt).getAsInt()).toArray()));
                    }
                }
            } else if (desiredClass == LongArrayTag.class) {
                for (Tag element : elements) {
                    if (element instanceof LongArrayTag) {
                        listNBT.add(element);
                    } else if (element instanceof IntArrayTag tag) {
                        listNBT.add(new LongArrayTag(Arrays.stream(tag.getAsIntArray()).asLongStream().toArray()));
                    } else if (element instanceof ByteArrayTag tag) {
                        byte[] values = tag.getAsByteArray();
                        listNBT.add(new LongArrayTag(IntStream.range(0, values.length).map(i -> values[i]).asLongStream().toArray()));
                    } else if (element instanceof CompoundTag) {//Empty compound
                        listNBT.add(new LongArrayTag(new long[0]));
                    } else if (element instanceof ListTag tag) {//List of shorts or empty list
                        listNBT.add(new LongArrayTag(tag.stream().mapToLong(nbt -> ((ShortTag) nbt).getAsLong()).toArray()));
                    }
                }
            } else if (desiredClass == ListTag.class) {
                for (Tag element : elements) {
                    if (element instanceof ListTag) {
                        listNBT.add(element);
                    } else if (element instanceof CompoundTag) {//Empty compound
                        listNBT.add(new ListTag());
                    } else if (element instanceof CollectionTag<?> tag) {//Byte int or long array
                        ListTag byteList = new ListTag();
                        byteList.addAll(tag);
                        listNBT.add(byteList);
                    }
                }
            } else {
                Collections.addAll(listNBT, elements);
            }
            return listNBT;
        }

        private static boolean validUpcast(Class<? extends Tag> argument, Class<? extends Tag> target) {
            if (argument == ByteTag.class) {
                return target == ShortTag.class || target == IntTag.class || target == LongTag.class || target == FloatTag.class || target == DoubleTag.class;
            } else if (argument == ShortTag.class) {
                return target == IntTag.class || target == LongTag.class || target == FloatTag.class || target == DoubleTag.class;
            } else if (argument == IntTag.class) {
                return target == LongTag.class || target == FloatTag.class || target == DoubleTag.class;
            } else if (argument == LongTag.class) {
                return target == FloatTag.class || target == DoubleTag.class;
            } else if (argument == FloatTag.class) {
                return target == DoubleTag.class;
            }
            return false;
        }
    }
}