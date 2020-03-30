package mekanism.common.content.gear;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleElectrolyticBreathingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleInhalationPurificationUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleRadiationShieldingUnit;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class Modules {

    private static final Map<String, ModuleData<?>> MODULES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Set<String>> SUPPORTED_MODULES = new Object2ObjectOpenHashMap<>();

    public static final ModuleData<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = register("electrolytic_breathing_unit", () -> new ModuleElectrolyticBreathingUnit());
    public static final ModuleData<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = register("inhalation_purification_unit", () -> new ModuleInhalationPurificationUnit());
    public static final ModuleData<ModuleRadiationShieldingUnit> RADIATION_SHIELDING_UNIT = register("radiation_shielding_unit", () -> new ModuleRadiationShieldingUnit());

    public static void setSupported(Item containerItem, ModuleData<?>... types) {
        for (ModuleData<?> module : types) {
            SUPPORTED_MODULES.computeIfAbsent(containerItem, item -> new HashSet<>()).add(module.getName());
        }
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
            return null;
        }

        CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
        return modulesTag.keySet().stream().map(name -> load(container, MODULES.get(name), modulesTag)).collect(Collectors.toList());
    }

    private static <M extends Module> ModuleData<M> register(String name, Supplier<M> moduleSupplier) {
        return register(name, moduleSupplier, 1);
    }

    private static <M extends Module> ModuleData<M> register(String name, Supplier<M> moduleSupplier, int maxStackSize) {
        ModuleData<M> data = new ModuleData<M>(name, moduleSupplier, maxStackSize);
        MODULES.put(name, data);
        return data;
    }

    public static class ModuleData<MODULE extends Module> {
        private String name;
        private Supplier<MODULE> supplier;
        private int maxStackSize;

        private ModuleData(String name, Supplier<MODULE> supplier, int maxStackSize) {
            this.name = name;
            this.supplier = supplier;
            this.maxStackSize = maxStackSize;
        }

        public int getMaxStackSize() {
            return maxStackSize;
        }

        public MODULE get(ItemStack container) {
            MODULE module = supplier.get();
            module.init(name, container);
            return module;
        }

        public String getName() {
            return name;
        }
    }
}
