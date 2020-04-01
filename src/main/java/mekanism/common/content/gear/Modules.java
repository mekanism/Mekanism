package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.NBTConstants;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleElectrolyticBreathingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleInhalationPurificationUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleRadiationShieldingUnit;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Modules {

    private static final Map<String, ModuleData<?>> MODULES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Set<ModuleData<?>>> SUPPORTED_MODULES = new Object2ObjectOpenHashMap<>();

    public static final ModuleData<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = register("electrolytic_breathing_unit",
        MekanismLang.MODULE_ELECTROLYTIC_BREATHING_UNIT, MekanismLang.DESCRIPTION_ELECTROLYTIC_BREATHING_UNIT, () -> new ModuleElectrolyticBreathingUnit());
    public static final ModuleData<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = register("inhalation_purification_unit",
        MekanismLang.MODULE_INHALATION_PURIFICATION_UNIT, MekanismLang.DESCRIPTION_INHALATION_PURIFICATION_UNIT, () -> new ModuleInhalationPurificationUnit());
    public static final ModuleData<ModuleRadiationShieldingUnit> RADIATION_SHIELDING_UNIT = register("radiation_shielding_unit",
        MekanismLang.MODULE_RADIATION_SHIELDING_UNIT, MekanismLang.DESCRIPTION_RADIATION_SHIELDING_UNIT, () -> new ModuleRadiationShieldingUnit());

    public static void setSupported(Item containerItem, ModuleData<?>... types) {
        for (ModuleData<?> module : types) {
            SUPPORTED_MODULES.computeIfAbsent(containerItem, item -> new HashSet<>()).add(module);
        }
    }

    public static Set<ModuleData<?>> getSupported(Item containerItem) {
        return SUPPORTED_MODULES.getOrDefault(containerItem, new HashSet<>());
    }

    public static <MODULE extends Module> MODULE load(ItemStack container, ModuleData<MODULE> type) {
        if (container == null || !(container.getItem() instanceof IModuleContainerItem)) {
            return null;
        }

        CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
        return load(container, type, modulesTag);
    }

    private static <MODULE extends Module> MODULE load(ItemStack container, ModuleData<MODULE> type, CompoundNBT modulesTag) {
        if (type == null || !modulesTag.contains(type.getName())) {
            return null;
        }

        MODULE module = type.get(container);
        if (module == null) {
            Mekanism.logger.error("Attempted to load unknown module type '" + type + "' from container " + container.getItem());
        }

        module.read(modulesTag.getCompound(type.getName()));
        return module;
    }

    public static List<Module> loadAll(ItemStack container) {
        if (container == null || !(container.getItem() instanceof IModuleContainerItem)) {
            return new ArrayList<>();
        }

        CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
        return modulesTag.keySet().stream().map(name -> load(container, MODULES.get(name), modulesTag)).collect(Collectors.toList());
    }

    private static <M extends Module> ModuleData<M> register(String name, ILangEntry langEntry, ILangEntry description, Supplier<M> moduleSupplier) {
        return register(name, langEntry, description, moduleSupplier, 1);
    }

    private static <M extends Module> ModuleData<M> register(String name, ILangEntry langEntry, ILangEntry description, Supplier<M> moduleSupplier, int maxStackSize) {
        ModuleData<M> data = new ModuleData<M>(name, langEntry, description, moduleSupplier, maxStackSize);
        MODULES.put(name, data);
        return data;
    }

    public static class ModuleData<MODULE extends Module> implements IHasTranslationKey {
        private String name;
        private ILangEntry langEntry;
        private ILangEntry description;
        private Supplier<MODULE> supplier;
        private int maxStackSize;
        private ItemStack stack;

        private ModuleData(String name, ILangEntry langEntry, ILangEntry description, Supplier<MODULE> supplier, int maxStackSize) {
            this.name = name;
            this.langEntry = langEntry;
            this.description = description;
            this.supplier = supplier;
            this.maxStackSize = maxStackSize;
        }

        public int getMaxStackSize() {
            return maxStackSize;
        }

        public MODULE get(ItemStack container) {
            MODULE module = supplier.get();
            module.init(this, container);
            return module;
        }

        public void setStack(Item item) {
            this.stack = new ItemStack(item);
        }

        public ItemStack getStack() {
            return stack;
        }

        public String getName() {
            return name;
        }

        public ITextComponent getDescription() {
            return new TranslationTextComponent(description.getTranslationKey());
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }
    }
}
