package mekanism.common.registration.impl;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ContainerTypeRegistryObject<CONTAINER extends AbstractContainerMenu> extends MekanismDeferredHolder<MenuType<?>, MenuType<CONTAINER>> {

    public ContainerTypeRegistryObject(Identifier key) {
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
    public MenuProvider getProvider(ILangEntry name, InteractionHand hand, ItemResource itemType) {
        return getProvider(name.translate(), hand, itemType);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemResource itemType) {
        return getProvider(name, hand, itemType, false);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemResource itemType, boolean resetMousePosition) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismItemContainerType<?> mekanismItemContainerType) {
            constructor = mekanismItemContainerType.create(hand, itemType);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create container for type: {}", getId());
        }
        return constructor == null ? null : new ContainerProvider(name, constructor, resetMousePosition);
    }

    public void tryOpenGui(Player player, InteractionHand hand) {
        tryOpenGui(player, hand, ItemAccessUtils.playerHandAccess(player, hand));
    }

    public void tryOpenGui(Player player, InteractionHand hand, ItemAccess itemAccess, TransactionContext ignored) {
        tryOpenGui(player, hand, itemAccess);
    }

    public void tryOpenGui(Player player, InteractionHand hand, ItemAccess itemAccess) {
        ItemResource itemType = itemAccess.getResource();
        MenuProvider provider = getProvider(itemType.getHoverName(), hand, itemType, true);
        if (provider != null) {
            //Validate the provider isn't null, it shouldn't be but just in case
            player.openMenu(provider, buf -> {
                buf.writeEnum(hand);
                if (itemType.getItem() instanceof IGuiItem guiItem) {//Should always be the case
                    guiItem.encodeContainerData(buf, itemType);
                }
            });
        }
    }
}