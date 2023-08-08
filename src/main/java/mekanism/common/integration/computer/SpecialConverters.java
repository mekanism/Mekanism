package mekanism.common.integration.computer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.*;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.text.InputValidator;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpecialConverters {

    @Nullable
    public static <ENUM extends Enum<?>> ENUM sanitizeStringToEnum(Class<? extends ENUM> expectedType, String argument) {
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

    private static ItemStack tryCreateFilterItem(@Nullable Object rawName, @Nullable Object rawNBT) throws ComputerException {
        Item item = tryCreateItem(rawName);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (rawNBT instanceof String snbt) {
            try {
                stack.setTag(NbtUtils.snbtToStructure(snbt));
            } catch (CommandSyntaxException ex) {
                throw new ComputerException("Invalid SNBT: "+ex.getMessage());
            }
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
        return raw instanceof Boolean bool ? bool : false;
    }

    private static int getIntFromRaw(@Nullable Object raw) {
        return raw instanceof Number number ? number.intValue() : 0;
    }

    @Nullable
    public static <FILTER extends IFilter<FILTER>> FILTER convertMapToFilter(Class<FILTER> expectedType, Map<?, ?> map) throws ComputerException {
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
                    Object enabled = map.get("enabled");
                    if (enabled instanceof Boolean enable) {
                        filter.setEnabled(enable);
                    }
                    if (filter instanceof IItemStackFilter<?> itemFilter) {
                        ItemStack stack = tryCreateFilterItem(map.get("item"), map.get("itemNBT"));
                        if (stack.isEmpty()) {
                            return null;
                        }
                        itemFilter.setItemStack(stack);
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
                    //noinspection unchecked
                    return (FILTER) filter;
                }
            }
        }
        return null;
    }

    static Map<String, Object> wrapStack(ResourceLocation name, String sizeKey, int amount, @Nullable CompoundTag tag) {
        boolean hasTag = tag != null && !tag.isEmpty() && amount > 0;
        Map<String, Object> wrapped = new HashMap<>(hasTag ? 3 : 2);
        wrapped.put("name", name == null ? "unknown" : name.toString());
        wrapped.put(sizeKey, amount);
        if (hasTag) {
            wrapped.put("nbt", wrapNBT(tag));
        }
        return wrapped;
    }

    static String wrapNBT(@NotNull CompoundTag nbt) {
        return NbtUtils.structureToSnbt(nbt);
    }

}
