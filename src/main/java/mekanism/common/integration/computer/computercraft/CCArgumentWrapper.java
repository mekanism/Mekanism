package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.integration.computer.ComputerArgumentHandler;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CCArgumentWrapper extends ComputerArgumentHandler<LuaException, MethodResult> {

    private static final double MAX_FLOATING_LONG_AS_DOUBLE = Double.parseDouble(FloatingLong.MAX_VALUE.toString());
    private final IArguments arguments;

    public CCArgumentWrapper(IArguments arguments) {
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
                        //Note: MIN_VALUE on float is smallest positive number
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
            }
        } else if (argument instanceof Map) {
            //Note: instanceof has slightly better performance than if we would instead fo Map.class.isAssignableFrom(argumentType)
            if (IFilter.class.isAssignableFrom(expectedType)) {
                Object sanitized = convertMapToFilter(expectedType, (Map<?, ?>) argument);
                if (sanitized != null) {
                    return sanitized;
                }
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
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getFluid()));
            wrapped.put("amount", stack.getAmount());
            return wrapped;
        } else if (result instanceof ItemStack) {
            ItemStack stack = (ItemStack) result;
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getItem()));
            wrapped.put("count", stack.getCount());
            return wrapped;
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
                wrapped.put("item", wrapReturnType(((IItemStackFilter<?>) result).getItemStack().getItem()));
            } else if (result instanceof IMaterialFilter) {
                wrapped.put("materialItem", wrapReturnType(((IMaterialFilter<?>) result).getMaterialItem().getItem()));
            } else if (result instanceof IModIDFilter) {
                wrapped.put("modId", ((IModIDFilter<?>) result).getModID());
            } else if (result instanceof ITagFilter) {
                wrapped.put("tag", ((ITagFilter<?>) result).getTagName());
            }
            if (result instanceof MinerFilter) {
                MinerFilter<?> minerFilter = (MinerFilter<?>) result;
                wrapped.put("requireReplace", minerFilter.requireStack);
                wrapped.put("replaceItem", wrapReturnType(minerFilter.replaceStack.getItem()));
            } else if (result instanceof SorterFilter) {
                SorterFilter<?> sorterFilter = (SorterFilter<?>) result;
                wrapped.put("allowDefault", sorterFilter.allowDefault);
                wrapped.put("color", wrapReturnType(sorterFilter.color));
                if (sorterFilter instanceof SorterItemStackFilter) {
                    SorterItemStackFilter filter = (SorterItemStackFilter) sorterFilter;
                    wrapped.put("fuzzy", filter.fuzzyMode);
                    //TODO - 10.1: Move size information to main sorter filter
                    wrapped.put("size", filter.sizeMode);
                    wrapped.put("min", filter.min);
                    wrapped.put("max", filter.max);
                }
            } /*else if (result instanceof QIOFilter) {
                //No specifics QIO only extra data
            }*/ else if (result instanceof OredictionificatorFilter) {
                OredictionificatorFilter filter = (OredictionificatorFilter) result;
                wrapped.put("target", filter.getFilterText());
                wrapped.put("selected", filter.getIndex());
            }
            return wrapped;
        } else if (result instanceof Map) {
            return ((Map<?, ?>) result).entrySet().stream().collect(Collectors.toMap(entry -> wrapReturnType(entry.getKey()), entry -> wrapReturnType(entry.getValue()),
                  (a, b) -> b));
        } else if (result instanceof Collection) {
            //Note: We support any "collection" as it doesn't really matter if it is a set vs a list because
            // on ComputerCraft's end it will just be converted from a collection to a table and it be iterated
            // so there is no real difference at that point about the type it is
            return ((Collection<?>) result).stream().map(CCArgumentWrapper::wrapReturnType).collect(Collectors.toList());
        } else if (result instanceof Object[]) {
            //Note: This doesn't handle/deal with primitive arrays
            return Arrays.stream((Object[]) result).map(CCArgumentWrapper::wrapReturnType).toArray();
        }
        return result;
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
                    // but as all of the enums we are using are all capital, this should not matter
                    return enumConstant;
                }
            }
        }
        return null;
    }

    private static ItemStack tryCreateFilterItem(@Nullable Object rawName) {
        if (rawName instanceof String) {
            ResourceLocation itemName = ResourceLocation.tryParse((String) rawName);
            if (itemName != null) {
                Item item = ForgeRegistries.ITEMS.getValue(itemName);
                if (item != null && item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    private static String tryGetFilterTag(@Nullable Object rawTag) {
        if (rawTag instanceof String) {
            String tag = (String) rawTag;
            if (!tag.isEmpty()) {
                //TODO - 10.1: Evaluate adding some extra validation here such as pertaining to capitals or other restrictions
                return tag;
            }
        }
        return null;
    }

    @Nullable
    private static String tryGetFilterModId(@Nullable Object rawModId) {
        if (rawModId instanceof String) {
            String modId = (String) rawModId;
            if (!modId.isEmpty()) {
                //TODO - 10.1: Evaluate adding some extra validation here such as pertaining to capitals or other restrictions
                return modId;
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
            //Handle filters as arguments, this may not be the best implementation but it will do for now
            FilterType filterType = sanitizeStringToEnum(FilterType.class, (String) type);
            if (filterType != null) {
                IFilter<?> filter = BaseFilter.fromType(filterType);
                if (expectedType.isInstance(filter)) {
                    //Validate the filter is of the type we expect
                    if (filter instanceof IItemStackFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("item"));
                        if (stack.isEmpty()) {
                            return null;
                        }
                        ((IItemStackFilter<?>) filter).setItemStack(stack);
                    } else if (filter instanceof IMaterialFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("materialItem"));
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
                        minerFilter.requireStack = getBooleanFromRaw(map.get("requireReplace"));
                        minerFilter.replaceStack = tryCreateFilterItem(map.get("replaceItem"));
                    } else if (filter instanceof SorterFilter) {
                        SorterFilter<?> sorterFilter = (SorterFilter<?>) filter;
                        sorterFilter.allowDefault = getBooleanFromRaw(map.get("allowDefault"));
                        Object rawColor = map.get("color");
                        if (rawColor instanceof String) {
                            sorterFilter.color = sanitizeStringToEnum(EnumColor.class, (String) rawColor);
                        }
                        if (sorterFilter instanceof SorterItemStackFilter) {
                            SorterItemStackFilter sorterItemFilter = (SorterItemStackFilter) sorterFilter;
                            sorterItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
                            //TODO - 10.1: Move size information to main sorter filter
                            sorterItemFilter.sizeMode = getBooleanFromRaw(map.get("size"));
                            sorterItemFilter.min = getIntFromRaw(map.get("min"));
                            sorterItemFilter.max = getIntFromRaw(map.get("max"));
                            if (sorterItemFilter.min < 0 || sorterItemFilter.max < 0 || sorterItemFilter.min > sorterItemFilter.max || sorterItemFilter.max > 64) {
                                return null;
                            }
                        }
                    } /*else if (filter instanceof QIOFilter) {
                        //No specifics QIO only extra data
                    }*/ else if (filter instanceof OredictionificatorFilter) {
                        OredictionificatorFilter oredictionificatorFilter = (OredictionificatorFilter) filter;
                        Object rawTag = map.get("tag");
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
                        ItemStack stack = tryCreateFilterItem(map.get("selected"));
                        if (!stack.isEmpty()) {
                            oredictionificatorFilter.setSelectedOutput(stack.getItem());
                        }
                    }
                    return filter;
                }
            }
        }
        return null;
    }
}