package mekanism.common.registration.impl;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ContainerTypeRegistryObject<CONTAINER extends AbstractContainerMenu> extends MekanismDeferredHolder<MenuType<?>, MenuType<CONTAINER>> {

    public ContainerTypeRegistryObject(ResourceLocation key) {
        this(ResourceKey.create(Registries.MENU, key));
    }

    public ContainerTypeRegistryObject(ResourceKey<MenuType<?>> key) {
        super(key);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, Object object) {
        return getProvider(name, object, false);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, Object object, boolean resetMousePosition) {
        return getProvider(name.translate(), object, resetMousePosition);
    }

    @Nullable
    public MenuProvider getProvider(Component name, Object object, boolean resetMousePosition) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
            constructor = mekanismContainerType.create(object);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create container for type: {}", getId());
        }
        return constructor == null ? null : new ContainerProvider(name, constructor, resetMousePosition);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, InteractionHand hand, ItemStack stack) {
        return getProvider(name.translate(), hand, stack);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemStack stack) {
        return getProvider(name, hand, stack, false);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemStack stack, boolean resetMousePosition) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismItemContainerType<?, ?> mekanismItemContainerType) {
            constructor = mekanismItemContainerType.create(hand, stack);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create container for type: {}", getId());
        }
        return constructor == null ? null : new ContainerProvider(name, constructor, resetMousePosition);
    }

    public void tryOpenGui(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        MenuProvider provider = getProvider(stack.getHoverName(), hand, stack, true);
        if (provider != null) {
            //Validate the provider isn't null, it shouldn't be but just in case
            player.openMenu(provider, buf -> {
                buf.writeEnum(hand);
                ItemStack.STREAM_CODEC.encode(buf, stack);
                if (stack.getItem() instanceof IGuiItem guiItem) {//Should always be the case
                    guiItem.encodeContainerData(buf, stack);
                }
            });
        }
    }
}