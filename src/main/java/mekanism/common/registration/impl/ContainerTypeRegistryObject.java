package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.NetworkHooks;

public class ContainerTypeRegistryObject<CONTAINER extends Container> extends WrappedRegistryObject<ContainerType<CONTAINER>> {

    public ContainerTypeRegistryObject(RegistryObject<ContainerType<CONTAINER>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public ContainerType<CONTAINER> getContainerType() {
        return get();
    }

    //Internal use only overwrite the registry object
    ContainerTypeRegistryObject<CONTAINER> setRegistryObject(RegistryObject<ContainerType<CONTAINER>> registryObject) {
        this.registryObject = registryObject;
        return this;
    }

    @Nullable
    public INamedContainerProvider getProvider(ILangEntry name, Object object) {
        return getProvider(name.translate(), object);
    }

    @Nullable
    public INamedContainerProvider getProvider(ITextComponent name, Object object) {
        IContainerProvider provider = null;
        ContainerType<CONTAINER> containerType = get();
        if (containerType instanceof MekanismContainerType) {
            provider = ((MekanismContainerType<?, CONTAINER>) containerType).create(object);
        }
        if (provider == null) {
            Mekanism.logger.info("Unable to create container for type: {}", containerType.getRegistryName());
        }
        return provider == null ? null : new ContainerProvider(name, provider);
    }

    @Nullable
    public INamedContainerProvider getProvider(ILangEntry name, Hand hand, ItemStack stack) {
        return getProvider(name.translate(), hand, stack);
    }

    @Nullable
    public INamedContainerProvider getProvider(ITextComponent name, Hand hand, ItemStack stack) {
        IContainerProvider provider = null;
        ContainerType<CONTAINER> containerType = get();
        if (containerType instanceof MekanismItemContainerType) {
            provider = ((MekanismItemContainerType<?, ?>) containerType).create(hand, stack);
        }
        if (provider == null) {
            Mekanism.logger.info("Unable to create container for type: {}", containerType.getRegistryName());
        }
        return provider == null ? null : new ContainerProvider(name, provider);
    }

    public void tryOpenGui(ServerPlayerEntity player, Hand hand, ItemStack stack) {
        INamedContainerProvider provider = getProvider(stack.getHoverName(), hand, stack);
        if (provider != null) {
            //Validate the provider isn't null, it shouldn't be but just in case
            NetworkHooks.openGui(player, provider, buf -> {
                buf.writeEnum(hand);
                buf.writeItem(stack);
            });
        }
    }
}