package mekanism.common.content.gear;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IHasConditionalAttributes;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModuleContainerItem extends IModeItem, IItemHUDProvider, IHasConditionalAttributes {

    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> IModuleContainer moduleContainer(ITEM instance) {
        return IModuleHelper.INSTANCE.getModuleContainer(instance);
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> Collection<? extends IModule<?>> getModules(ITEM instance) {
        return IModuleHelper.INSTANCE.getAllModules(instance);
    }

    @Override
    default void adjustAttributes(ItemAttributeModifierEvent event) {
        for (IModule<?> module : getModules(event.getItemStack())) {
            if (module.isEnabled()) {
                adjustAttributes(module, event);
            }
        }
    }

    private <MODULE extends ICustomModule<MODULE>> void adjustAttributes(IModule<MODULE> module, ItemAttributeModifierEvent event) {
        module.getCustomInstance().adjustAttributes(module, event);
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean hasInstalledModules(ITEM instance) {
        IModuleContainer container = moduleContainer(instance);
        return container != null && container.installedCount() > 0;
    }

    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ITEM instance,
          DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        return IModuleHelper.INSTANCE.getIfEnabled(instance, type);
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> void addModuleDetails(ITEM instance, Consumer<Component> tooltipAdder) {
        for (IModule<?> module : getModules(instance)) {
            ModuleData<?> data = module.getUntypedData();
            if (module.getInstalledCount() > 1) {
                Component amount = MekanismLang.GENERIC_FRACTION.translate(module.getInstalledCount(), data.getMaxStackSize());
                tooltipAdder.accept(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, data, amount));
            } else {
                tooltipAdder.accept(TextComponentUtil.build(EnumColor.GRAY, data));
            }
        }
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean hasModule(ITEM instance, Holder<ModuleData<?>> type) {
        IModuleContainer container = moduleContainer(instance);
        return container != null && container.has(type);
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean isModuleEnabled(ITEM instance, Holder<ModuleData<?>> type) {
        return IModuleHelper.INSTANCE.isEnabled(instance, type);
    }

    @Override
    default void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        IModuleContainer moduleContainer = moduleContainer(stack);
        if (moduleContainer != null) {
            list.addAll(moduleContainer.getHUDStrings(player, stack));
        }
    }

    @Override
    default void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        IModuleContainer moduleContainer = moduleContainer(stack);
        if (moduleContainer != null) {
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.handlesModeChange()) {
                    changeMode(module, player, moduleContainer, stack, shift, displayChange);
                    return;
                }
            }
        }
    }

    @Override
    default boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        if (!IModeItem.super.supportsSlotType(stack, slotType)) {
            return false;
        }
        for (IModule<?> iModule : getModules(stack)) {
            if (iModule.handlesAnyModeChange()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    default Component getScrollTextComponent(@NotNull ItemStack stack) {
        for (IModule<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                return getModeScrollComponent(module, stack);
            }
        }
        return null;
    }

    private static <MODULE extends ICustomModule<MODULE>> void changeMode(IModule<MODULE> module, Player player, IModuleContainer moduleContainer, ItemStack stack,
          int shift, DisplayChange displayChange) {
        module.getCustomInstance().changeMode(module, player, moduleContainer, stack, shift, displayChange != DisplayChange.NONE);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> Component getModeScrollComponent(IModule<MODULE> module, ItemStack stack) {
        return module.getCustomInstance().getModeScrollComponent(module, stack);
    }
}