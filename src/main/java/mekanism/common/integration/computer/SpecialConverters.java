package mekanism.common.integration.computer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.text.InputValidator;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static ItemStack tryCreateFilterItem(@Nullable String rawName, @Nullable String rawComponents) throws ComputerException {
        Item item = tryCreateItem(rawName);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        if (rawComponents != null) {
            DataComponentPatch dataComponents = unwrapComponents(rawComponents);
            return new ItemStack(item.builtInRegistryHolder(), 1, dataComponents);
        }
        return new ItemStack(item);
    }

    private static Item tryCreateItem(@Nullable Object rawName) {
        if (rawName instanceof String name) {
            ResourceLocation itemName = ResourceLocation.tryParse(name);
            if (itemName != null) {
                return BuiltInRegistries.ITEM.get(itemName);
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

    static int getIntFromRaw(@Nullable Object raw) {
        return raw instanceof Number number ? number.intValue() : 0;
    }

    @NotNull
    public static <FILTER extends IFilter<FILTER>> FILTER convertMapToFilter(@NotNull Class<FILTER> expectedType, @NotNull Map<?, ?> map) throws ComputerException {
        //We may want to try improving this at some point, or somehow making it slightly less hardcoded
        // but for now this will have to do
        Object type = map.get(SerializationConstants.TYPE);
        if (!(type instanceof String string)) {
            throw new ComputerException("Missing '" + SerializationConstants.TYPE + "' element");
        }
        //Handle filters as arguments, this may not be the best implementation, but it will do for now
        FilterType filterType = sanitizeStringToEnum(FilterType.class, string);
        if (filterType == null) {
            throw new ComputerException("Unknown '" + SerializationConstants.TYPE + "' value");
        }
        IFilter<?> filter = BaseFilter.fromType(filterType);
        if (!expectedType.isInstance(filter)) {
            throw new ComputerException("Unexpected format for: " + SerializationConstants.TYPE);
        }
        //Validate the filter is of the type we expect
        Object enabled = map.get(SerializationConstants.ENABLED);
        if (enabled instanceof Boolean enable) {
            filter.setEnabled(enable);
        }
        switch (filter) {
            case IItemStackFilter<?> itemFilter -> decodeItemStackFilter(map, itemFilter);
            case IModIDFilter<?> modIDFilter -> decodeModIdFilter(map, modIDFilter);
            case ITagFilter<?> tagFilter -> decodeTagFilter(map, tagFilter);
            default -> {
            }
        }
        switch (filter) {
            case MinerFilter<?> minerFilter -> decodeMinerFilter(map, minerFilter);
            case SorterFilter<?> sorterFilter -> decodeSorterFilter(map, sorterFilter);
            case QIOFilter<?> qioFilter -> decodeQioFilter(map, qioFilter);
            case OredictionificatorFilter<?, ?, ?> oredictionificatorFilter -> decodeOreDictFilter(map, oredictionificatorFilter);
            default -> {
            }
        }
        return expectedType.cast(filter);
    }

    private static void decodeOreDictFilter(@NotNull Map<?, ?> map, OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) throws ComputerException {
        Object rawTag = map.get(SerializationConstants.TARGET);
        if (!(rawTag instanceof String tag) || tag.isEmpty()) {
            throw new ComputerException("Missing '" + SerializationConstants.TARGET + "'");
        }
        ResourceLocation rl = ResourceLocation.tryParse(tag);
        if (rl == null || !TileEntityOredictionificator.isValidTarget(rl)) {
            throw new ComputerException("Invalid '" + SerializationConstants.TARGET + "'");
        }
        oredictionificatorFilter.setFilter(rl);
        if (oredictionificatorFilter instanceof OredictionificatorItemFilter itemFilter) {
            Item item = tryCreateItem(map.get(SerializationConstants.SELECTED));
            if (item != Items.AIR) {
                itemFilter.setSelectedOutput(item.builtInRegistryHolder());
            }
        }
    }

    private static void decodeQioFilter(@NotNull Map<?, ?> map, QIOFilter<?> qioFilter) {
        if (qioFilter instanceof QIOItemStackFilter qioItemFilter) {
            qioItemFilter.fuzzyMode = getBooleanFromRaw(map.get(SerializationConstants.FUZZY));
        }
    }

    private static void decodeSorterFilter(@NotNull Map<?, ?> map, SorterFilter<?> sorterFilter) throws ComputerException {
        sorterFilter.allowDefault = getBooleanFromRaw(map.get(SerializationConstants.ALLOW_DEFAULT));
        Object rawColor = map.get(SerializationConstants.COLOR);
        if (rawColor instanceof String) {
            sorterFilter.color = sanitizeStringToEnum(EnumColor.class, (String) rawColor);
        }
        sorterFilter.sizeMode = getBooleanFromRaw(map.get(SerializationConstants.SIZE));
        sorterFilter.min = getIntFromRaw(map.get(SerializationConstants.MIN));
        sorterFilter.max = getIntFromRaw(map.get(SerializationConstants.MAX));
        if (sorterFilter.min < 0 || sorterFilter.max < 0 || sorterFilter.min > sorterFilter.max || sorterFilter.max > Item.ABSOLUTE_MAX_STACK_SIZE) {
            throw new ComputerException("Invalid " + SerializationConstants.MIN + "/" + SerializationConstants.MAX + ": 0 <= " + SerializationConstants.MIN +
                                        " <= " + SerializationConstants.MAX + " <= " + Item.ABSOLUTE_MAX_STACK_SIZE);
        }
        if (sorterFilter instanceof SorterItemStackFilter sorterItemFilter) {
            sorterItemFilter.fuzzyMode = getBooleanFromRaw(map.get(SerializationConstants.FUZZY));
        }
    }

    private static void decodeMinerFilter(@NotNull Map<?, ?> map, MinerFilter<?> minerFilter) {
        minerFilter.requiresReplacement = getBooleanFromRaw(map.get(SerializationConstants.REQUIRES_REPLACEMENT));
        minerFilter.replaceTarget = tryCreateItem(map.get(SerializationConstants.REPLACE_TARGET));
    }

    private static void decodeTagFilter(@NotNull Map<?, ?> map, ITagFilter<?> tagFilter) throws ComputerException {
        String tag = tryGetFilterTag(map.get(SerializationConstants.TAG));
        if (tag == null) {
            throw new ComputerException("Invalid or missing tag specified for Tag filter");
        }
        tagFilter.setTagName(tag);
    }

    private static void decodeModIdFilter(@NotNull Map<?, ?> map, IModIDFilter<?> modIDFilter) throws ComputerException {
        String modId = tryGetFilterModId(map.get(SerializationConstants.MODID));
        if (modId == null) {
            throw new ComputerException("Invalid or missing modId specified for Mod Id filter");
        }
        modIDFilter.setModID(modId);
    }

    private static void decodeItemStackFilter(@NotNull Map<?, ?> map, IItemStackFilter<?> itemFilter) throws ComputerException {
        ItemStack stack = tryCreateFilterItem((String) map.get(SerializationConstants.ITEM), (String) map.get(SerializationConstants.COMPONENTS));
        if (stack.isEmpty()) {
            throw new ComputerException("Invalid or missing item specified for ItemStack filter");
        }
        itemFilter.setItemStack(stack);
    }

    static Map<String, Object> wrapStack(String name, String sizeKey, int amount, @NotNull DataComponentPatch components) {
        int elements = 2;
        boolean hasComponents = !components.isEmpty() && amount > 0;
        if (hasComponents) {
            elements++;
        }
        Map<String, Object> wrapped = new HashMap<>(elements);
        wrapped.put(SerializationConstants.NAME, name);
        wrapped.put(sizeKey, amount);
        if (hasComponents) {
            wrapped.put(SerializationConstants.COMPONENTS, wrapComponents(components));
        }
        return wrapped;
    }

    static String wrapComponents(@NotNull DataComponentPatch components) {
        return NbtUtils.structureToSnbt((CompoundTag) DataComponentPatch.CODEC.encodeStart(getRegistryNbtOps(), components).getOrThrow());
    }

    static DataComponentPatch unwrapComponents(@NotNull String rawComponents) throws ComputerException {
        CompoundTag nbt;
        try {
            nbt = NbtUtils.snbtToStructure(rawComponents);
        } catch (CommandSyntaxException ex) {
            throw new ComputerException("Invalid SNBT: " + ex.getMessage());
        }
        return DataComponentPatch.CODEC.parse(getRegistryNbtOps(), nbt).getOrThrow(ComputerException::new);
    }

    private static DynamicOps<Tag> getRegistryNbtOps() {
        //TODO: Can we pass the registry access in from the tiles rather than having to look it up from the current server?
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        }
        return NbtOps.INSTANCE;
    }
}
