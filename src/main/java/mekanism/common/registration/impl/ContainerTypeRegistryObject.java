package mekanism.common.registration.impl;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ContainerProvider.ClientSideData;
import mekanism.common.inventory.container.EntityContainerProvider;
import mekanism.common.inventory.container.ItemContainerProvider;
import mekanism.common.inventory.container.TileContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
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
    public MenuProvider getProvider(ILangEntry name, Entity entity) {
        return getProvider(name, entity, false);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, Entity entity, boolean resetMousePosition) {
        return getProvider(name.translate(), entity, resetMousePosition, null);
    }

    @Nullable
    public MenuProvider getProvider(Component name, Entity entity, boolean resetMousePosition, @Nullable ClientSideData clientSideData) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
            constructor = mekanismContainerType.create(entity);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create entity container for type: {}", getId());
        }
        return constructor == null ? null : new EntityContainerProvider(name, constructor, resetMousePosition, clientSideData, entity);
    }

    @Nullable
    public MenuProvider getProviderNoTileData(ILangEntry name, TileEntityMekanism tile) {
        return getProvider(name.translate(), tile, false, null);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, TileEntityMekanism tile) {
        return getProvider(name.translate(), tile, false);
    }

    @Nullable
    public MenuProvider getProvider(Component name, TileEntityMekanism tile, boolean resetMousePosition) {
        return getProvider(name, tile, resetMousePosition, (_, _, buffer) -> tile.encodeExtraContainerData(buffer));
    }

    @Nullable
    public MenuProvider getProvider(Component name, TileEntityMekanism tile, boolean resetMousePosition, @Nullable ClientSideData clientSideData) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
            constructor = mekanismContainerType.create(tile);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create tile container for type: {}", getId());
        }
        return constructor == null ? null : new TileContainerProvider(name, constructor, resetMousePosition, clientSideData, tile);
    }

    @Nullable
    public MenuProvider getProviderNoItemData(ILangEntry name, InteractionHand hand, ItemResource itemType) {
        return getProvider(name.translate(), hand, itemType, false, null);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemResource itemType) {
        return getProvider(name, hand, itemType, false);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemResource itemType, boolean resetMousePosition) {
        ClientSideData clientSideData;
        if (itemType.getItem() instanceof IGuiItem guiItem) {//Should always be the case
            clientSideData = (_, _, buffer) -> guiItem.encodeContainerData(buffer, itemType);
        } else {
            clientSideData = null;
        }
        return getProvider(name, hand, itemType, resetMousePosition, clientSideData);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemResource itemType, boolean resetMousePosition, @Nullable ClientSideData clientSideData) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismItemContainerType<?> mekanismItemContainerType) {
            constructor = mekanismItemContainerType.create(hand, itemType);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create item backed container for type: {}", getId());
        }
        return constructor == null ? null : new ItemContainerProvider(name, constructor, resetMousePosition, clientSideData, hand);
    }

    public void tryOpenGui(Player player, InteractionHand hand) {
        tryOpenGui(player, hand, ItemAccessUtils.playerHandAccess(player, hand));
    }

    public void tryOpenGui(Player player, InteractionHand hand, ItemAccess itemAccess, TransactionContext ignored) {
        tryOpenGui(player, hand, itemAccess);
    }

    public void tryOpenGui(Player player, InteractionHand hand, ItemAccess itemAccess) {
        ItemResource itemType = itemAccess.getResource();
        player.openMenu(getProvider(itemType.getHoverName(), hand, itemType, true));
    }

    @Nullable
    public MenuProvider getProvider(Component name, Object object, boolean resetMousePosition, @Nullable ClientSideData clientSideData) {
        MenuConstructor constructor = null;
        if (get() instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
            constructor = mekanismContainerType.create(object);
        }
        if (constructor == null) {
            Mekanism.logger.info("Unable to create container for type: {}", getId());
        }
        return constructor == null ? null : new ContainerProvider(name, constructor, resetMousePosition, clientSideData);
    }
}