package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public interface IModuleContainerItem extends IItemHUDProvider {

    default List<Module<?>> getModules(ItemStack stack) {
        return ModuleHelper.INSTANCE.loadAll(stack);
    }

    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        return MekanismAPI.getModuleHelper().load(stack, typeProvider);
    }

    default boolean supportsModule(ItemStack stack, IModuleDataProvider<?> typeProvider) {
        return MekanismAPI.getModuleHelper().getSupported(stack).contains(typeProvider.getModuleData());
    }

    default void addModuleDetails(ItemStack stack, List<Component> tooltip) {
        for (Module<?> module : getModules(stack)) {
            ModuleData<?> data = module.getData();
            if (module.getInstalledCount() > 1) {
                Component amount = MekanismLang.GENERIC_FRACTION.translate(module.getInstalledCount(), data.getMaxStackSize());
                tooltip.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, data, amount));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.GRAY, data));
            }
        }
    }

    default boolean hasModule(ItemStack stack, IModuleDataProvider<?> type) {
        CompoundTag modules = ItemDataUtils.getCompound(stack, NBTConstants.MODULES);
        return modules.contains(type.getRegistryName().toString(), Tag.TAG_COMPOUND);
    }

    default boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
        IModule<?> module = getModule(stack, type);
        return module != null && module.isEnabled();
    }

    default void removeModule(ItemStack stack, ModuleData<?> type) {
        Module<?> module = ModuleHelper.INSTANCE.load(stack, type);
        if (module != null) {
            if (module.getInstalledCount() > 1) {
                module.setInstalledCount(module.getInstalledCount() - 1);
                module.save(null);
                module.onRemoved(false);
            } else {
                CompoundTag modules = ItemDataUtils.getCompound(stack, NBTConstants.MODULES);
                modules.remove(type.getRegistryName().toString());
                module.onRemoved(true);
            }
        }
    }

    default void addModule(ItemStack stack, ModuleData<?> type) {
        Module<?> module = ModuleHelper.INSTANCE.load(stack, type);
        if (module == null) {
            ItemDataUtils.getOrAddCompound(stack, NBTConstants.MODULES).put(type.getRegistryName().toString(), new CompoundTag());
            ModuleHelper.INSTANCE.load(stack, type).onAdded(true);
        } else {
            module.setInstalledCount(module.getInstalledCount() + 1);
            module.save(null);
            module.onAdded(false);
        }
    }

    @Override
    default void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        for (Module<?> module : getModules(stack)) {
            if (module.renderHUD()) {
                module.addHUDStrings(player, list);
            }
        }
    }

    default List<IHUDElement> getHUDElements(Player player, ItemStack stack) {
        List<IHUDElement> ret = new ArrayList<>();
        for (Module<?> module : getModules(stack)) {
            if (module.renderHUD()) {
                module.addHUDElements(player, ret);
            }
        }
        return ret;
    }

    static boolean hasOtherEnchants(ItemStack stack) {
        MatchedEnchants enchants = new MatchedEnchants(stack);
        forMatchingEnchants(stack, enchants, (e, module) -> e.matchedCount++);
        return enchants.enchantments == null || enchants.matchedCount < enchants.enchantments.size();
    }

    default void filterTooltips(ItemStack stack, List<Component> tooltips) {
        List<Component> enchantsToRemove = new ArrayList<>();
        forMatchingEnchants(stack, new MatchedEnchants(stack),
              (e, module) -> enchantsToRemove.add(module.getCustomInstance().getEnchantment().getFullname(module.getInstalledCount())));
        tooltips.removeAll(enchantsToRemove);
    }

    static void forMatchingEnchants(ItemStack stack, MatchedEnchants enchants, BiConsumer<MatchedEnchants, IModule<? extends EnchantmentBasedModule<?>>> consumer) {
        for (IModule<? extends EnchantmentBasedModule> module : MekanismAPI.getModuleHelper().loadAll(stack, EnchantmentBasedModule.class)) {
            if (module.isEnabled() && enchants.getEnchantments().getOrDefault(module.getCustomInstance().getEnchantment(), 0) == module.getInstalledCount()) {
                consumer.accept(enchants, (IModule<? extends EnchantmentBasedModule<?>>) module);
            }
        }
    }

    class MatchedEnchants {

        private final ItemStack stack;
        private Map<Enchantment, Integer> enchantments;
        private int matchedCount;

        public MatchedEnchants(ItemStack stack) {
            this.stack = stack;
        }

        public Map<Enchantment, Integer> getEnchantments() {
            if (enchantments == null) {
                enchantments = EnchantmentHelper.getEnchantments(stack);
            }
            return enchantments;
        }
    }
}