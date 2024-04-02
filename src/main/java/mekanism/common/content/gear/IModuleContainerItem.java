package mekanism.common.content.gear;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModuleContainerItem extends IModeItem, IItemHUDProvider {

    default Optional<? extends IModuleContainer> moduleContainer(ItemStack stack) {
        return IModuleHelper.INSTANCE.getModuleContainer(stack);
    }

    default Collection<? extends IModule<?>> getModules(ItemStack stack) {
        return moduleContainer(stack)
              .map(IModuleContainer::modules)
              .orElse(List.of());
    }

    default boolean hasInstalledModules(ItemStack stack) {
        return moduleContainer(stack)
              .filter(container -> container.installedCount() > 0)
              .isPresent();
    }

    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        return moduleContainer(stack)
              .map(container -> container.getIfEnabled(typeProvider))
              .orElse(null);
    }

    default void addModuleDetails(ItemStack stack, List<Component> tooltip) {
        for (IModule<?> module : getModules(stack)) {
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
        Optional<? extends IModuleContainer> container = moduleContainer(stack);
        //noinspection OptionalIsPresent - Capturing lambda
        if (container.isPresent()) {
            return container.get().has(type);
        }
        return false;
    }

    default boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
        Optional<? extends IModuleContainer> container = moduleContainer(stack);
        //noinspection OptionalIsPresent - Capturing lambda
        if (container.isPresent()) {
            return container.get().hasEnabled(type);
        }
        return false;
    }

    @Override
    default void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        moduleContainer(stack).ifPresent(container -> list.addAll(container.getHUDStrings(player)));
    }

    @Override
    default void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        for (IModule<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                changeMode(module, player, stack, shift, displayChange);
                return;
            }
        }
    }

    @Override
    default boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return IModeItem.super.supportsSlotType(stack, slotType) && getModules(stack).stream().anyMatch(IModule::handlesAnyModeChange);
    }

    @Nullable
    @Override
    default Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getModules(stack).stream()
              .filter(IModule::handlesModeChange)
              .findFirst()
              .map(module -> getModeScrollComponent(module, stack))
              .orElse(null);
    }

    private static <MODULE extends ICustomModule<MODULE>> void changeMode(IModule<MODULE> module, Player player, ItemStack stack, int shift, DisplayChange displayChange) {
        module.getCustomInstance().changeMode(module, player, stack, shift, displayChange != DisplayChange.NONE);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> Component getModeScrollComponent(IModule<MODULE> module, ItemStack stack) {
        return module.getCustomInstance().getModeScrollComponent(module, stack);
    }
}