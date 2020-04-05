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
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleChargeDistributionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleElectrolyticBreathingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleInhalationPurificationUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleNutritionalInjectionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleRadiationShieldingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleSolarRechargingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleVisionEnhancementUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleFarmingUnit;
import mekanism.common.content.gear.mekatool.ModuleMekaTool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleMekaTool.ModuleSilkTouchUnit;
import mekanism.common.content.gear.mekatool.ModuleMekaTool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleMekaTool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Modules {

    private static final Map<String, ModuleData<?>> MODULES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Set<ModuleData<?>>> SUPPORTED_MODULES = new Object2ObjectOpenHashMap<>();
    private static final Map<ModuleData<?>, Set<Item>> SUPPORTED_CONTAINERS = new Object2ObjectOpenHashMap<>();

    // Shared
    public static final ModuleData<ModuleEnergyUnit> ENERGY_UNIT = register("energy_unit",
        MekanismLang.MODULE_ENERGY_UNIT, MekanismLang.DESCRIPTION_ENERGY_UNIT, () -> new ModuleEnergyUnit(), 8)
        .setNoDisable();

    // Meka-Tool
    public static final ModuleData<ModuleExcavationEscalationUnit> EXCAVATION_ESCALATION_UNIT = register("excavation_escalation_unit",
        MekanismLang.MODULE_EXCAVATION_ESCALATION_UNIT, MekanismLang.DESCRIPTION_EXCAVATION_ESCALATION_UNIT, () -> new ModuleExcavationEscalationUnit(), 3)
        .setHandlesModeChange().setRendersHUD();
    public static final ModuleData<ModuleAttackAmplificationUnit> ATTACK_AMPLIFICATION_UNIT = register("attack_amplification_unit",
        MekanismLang.MODULE_ATTACK_AMPLIFICATION_UNIT, MekanismLang.DESCRIPTION_ATTACK_AMPLIFICATION_UNIT, () -> new ModuleAttackAmplificationUnit(), 3);
    public static final ModuleData<ModuleSilkTouchUnit> SILK_TOUCH_UNIT = register("silk_touch_unit",
        MekanismLang.MODULE_SILK_TOUCH_UNIT, MekanismLang.DESCRIPTION_SILK_TOUCH_UNIT, () -> new ModuleSilkTouchUnit());
    public static final ModuleData<ModuleVeinMiningUnit> VEIN_MINING_UNIT = register("vein_mining_unit",
        MekanismLang.MODULE_VEIN_MINING_UNIT, MekanismLang.DESCRIPTION_VEIN_MINING_UNIT, () -> new ModuleVeinMiningUnit());
    public static final ModuleData<ModuleFarmingUnit> FARMING_UNIT = register("farming_unit",
        MekanismLang.MODULE_FARMING_UNIT, MekanismLang.DESCRIPTION_FARMING_UNIT, () -> new ModuleFarmingUnit(), 4)
        .setExclusive();
    public static final ModuleData<ModuleTeleportationUnit> TELEPORTATION_UNIT = register("teleportation_unit",
        MekanismLang.MODULE_TELEPORTATION_UNIT, MekanismLang.DESCRIPTION_TELEPORTATION_UNIT, () -> new ModuleTeleportationUnit())
        .setExclusive();

    // Helmet
    public static final ModuleData<ModuleElectrolyticBreathingUnit> ELECTROLYTIC_BREATHING_UNIT = register("electrolytic_breathing_unit",
        MekanismLang.MODULE_ELECTROLYTIC_BREATHING_UNIT, MekanismLang.DESCRIPTION_ELECTROLYTIC_BREATHING_UNIT, () -> new ModuleElectrolyticBreathingUnit(), 4);
    public static final ModuleData<ModuleInhalationPurificationUnit> INHALATION_PURIFICATION_UNIT = register("inhalation_purification_unit",
        MekanismLang.MODULE_INHALATION_PURIFICATION_UNIT, MekanismLang.DESCRIPTION_INHALATION_PURIFICATION_UNIT, () -> new ModuleInhalationPurificationUnit());
    public static final ModuleData<ModuleRadiationShieldingUnit> RADIATION_SHIELDING_UNIT = register("radiation_shielding_unit",
        MekanismLang.MODULE_RADIATION_SHIELDING_UNIT, MekanismLang.DESCRIPTION_RADIATION_SHIELDING_UNIT, () -> new ModuleRadiationShieldingUnit());
    public static final ModuleData<ModuleVisionEnhancementUnit> VISION_ENHANCEMENT_UNIT = register("vision_enhancement_unit",
        MekanismLang.MODULE_VISION_ENHANCEMENT_UNIT, MekanismLang.DESCRIPTION_VISION_ENHANCEMENT_UNIT, () -> new ModuleVisionEnhancementUnit(), 4)
        .setHandlesModeChange().setRendersHUD().setDisabledByDefault();
    public static final ModuleData<ModuleSolarRechargingUnit> SOLAR_RECHARGING_UNIT = register("solar_recharging_unit",
        MekanismLang.MODULE_SOLAR_RECHARGING_UNIT, MekanismLang.DESCRIPTION_SOLAR_RECHARGING_UNIT, () -> new ModuleSolarRechargingUnit(), 8);
    public static final ModuleData<ModuleNutritionalInjectionUnit> NUTRITIONAL_INJECTION_UNIT = register("nutritional_injection_unit",
        MekanismLang.MODULE_NUTRITIONAL_INJECTION_UNIT, MekanismLang.DESCRIPTION_NUTRITIONAL_INJECTION_UNIT, () -> new ModuleNutritionalInjectionUnit())
        .setRendersHUD();

    // Chestplate
    public static final ModuleData<ModuleJetpackUnit> JETPACK_UNIT = register("jetpack_unit",
        MekanismLang.MODULE_JETPACK_UNIT, MekanismLang.DESCRIPTION_JETPACK_UNIT, () -> new ModuleJetpackUnit())
        .setHandlesModeChange().setRendersHUD().setExclusive();
    public static final ModuleData<ModuleGravitationalModulatingUnit> GRAVITATIONAL_MODULATING_UNIT = register("gravitational_modulating_unit",
        MekanismLang.MODULE_GRAVITATIONAL_MODULATING_UNIT, MekanismLang.DESCRIPTION_GRAVITATIONAL_MODULATING_UNIT, () -> new ModuleGravitationalModulatingUnit())
        .setHandlesModeChange().setRendersHUD().setExclusive();
    public static final ModuleData<ModuleChargeDistributionUnit> CHARGE_DISTRIBUTION_UNIT = register("charge_distribution_unit",
        MekanismLang.MODULE_CHARGE_DISTRIBUTION_UNIT, MekanismLang.DESCRIPTION_CHARGE_DISTRIBUTION_UNIT, () -> new ModuleChargeDistributionUnit());

    // Pants
    public static final ModuleData<ModuleLocomotiveBoostingUnit> LOCOMOTIVE_BOOSTING_UNIT = register("locomotive_boosting_unit",
        MekanismLang.MODULE_LOCOMOTIVE_BOOSTING_UNIT, MekanismLang.DESCRIPTION_LOCOMOTIVE_BOOSTING_UNIT, () -> new ModuleLocomotiveBoostingUnit());

    // Boots
    public static final ModuleData<ModuleHydraulicPropulsionUnit> HYDRAULIC_PROPULSION_UNIT = register("hydraulic_propulsion_unit",
        MekanismLang.MODULE_HYDRAULIC_PROPULSION_UNIT, MekanismLang.DESCRIPTION_HYDRAULIC_PROPULSION_UNIT, () -> new ModuleHydraulicPropulsionUnit());

    public static void setSupported(Item containerItem, ModuleData<?>... types) {
        for (ModuleData<?> module : types) {
            SUPPORTED_MODULES.computeIfAbsent(containerItem, item -> new HashSet<>()).add(module);
        }
    }

    public static ModuleData<?> get(String name) {
        return MODULES.get(name);
    }

    public static Set<ModuleData<?>> getSupported(ItemStack container) {
        return SUPPORTED_MODULES.getOrDefault(container.getItem(), new HashSet<>());
    }

    public static Set<Item> getSupported(ModuleData<?> type) {
        return SUPPORTED_CONTAINERS.getOrDefault(type, new HashSet<>());
    }

    public static <MODULE extends Module> MODULE load(ItemStack container, ModuleData<MODULE> type) {
        if (!(container.getItem() instanceof IModuleContainerItem)) {
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
        if (!(container.getItem() instanceof IModuleContainerItem)) {
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

    public static void processSupportedContainers() {
        for (Map.Entry<Item, Set<ModuleData<?>>> entry : SUPPORTED_MODULES.entrySet()) {
            for (ModuleData<?> data : entry.getValue()) {
                SUPPORTED_CONTAINERS.computeIfAbsent(data, d -> new HashSet<>()).add(entry.getKey());
            }
        }
    }

    public static void resetSupportedContainers() {
        SUPPORTED_CONTAINERS.clear();
    }

    public static class ModuleData<MODULE extends Module> implements IHasTranslationKey {
        private String name;
        private ILangEntry langEntry;
        private ILangEntry description;
        private Supplier<MODULE> supplier;
        private int maxStackSize;
        private ItemStack stack;

        /** Exclusive modules only work one-at-a-time; when one is enabled, others will be automatically disabled. */
        private boolean exclusive;
        private boolean handlesModeChange;
        private boolean rendersHUD;
        private boolean noDisable;
        private boolean disabledByDefault;

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

        public ILangEntry getLangEntry() {
            return langEntry;
        }

        public ModuleData<MODULE> setExclusive() {
            exclusive = true;
            return this;
        }

        public boolean isExclusive() {
            return exclusive;
        }

        public ModuleData<MODULE> setHandlesModeChange() {
            handlesModeChange = true;
            return this;
        }

        public boolean handlesModeChange() {
            return handlesModeChange;
        }

        public ModuleData<MODULE> setRendersHUD() {
            rendersHUD = true;
            return this;
        }

        public boolean rendersHUD() {
            return rendersHUD;
        }

        public ModuleData<MODULE> setNoDisable() {
            noDisable = true;
            return this;
        }

        public boolean isNoDisable() {
            return noDisable;
        }

        public ModuleData<MODULE> setDisabledByDefault() {
            disabledByDefault = true;
            return this;
        }

        public boolean isDisabledByDefault() {
            return disabledByDefault;
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }
    }
}
