package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeRegistryObject<BE extends BlockEntity> extends MekanismDeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> {

    private List<CapabilityData<BE, ?, ?>> capabilityProviders;
    @Nullable
    private BlockEntityTicker<BE> clientTicker;
    @Nullable
    private BlockEntityTicker<BE> serverTicker;

    public TileEntityTypeRegistryObject(ResourceLocation key) {
        this(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, key));
    }

    public TileEntityTypeRegistryObject(ResourceKey<BlockEntityType<?>> key) {
        super(key);
    }

    @Internal
    void clientTicker(BlockEntityTicker<BE> ticker) {
        if (clientTicker != null) {
            throw new IllegalStateException("Client ticker may only be set once.");
        }
        clientTicker = ticker;
    }

    @Internal
    void serverTicker(BlockEntityTicker<BE> ticker) {
        if (serverTicker != null) {
            throw new IllegalStateException("Server ticker may only be set once.");
        }
        serverTicker = ticker;
    }

    //TODO: Document use case of shouldApply
    <CAP, CONTEXT> void addCapability(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider, BooleanSupplier shouldApply) {
        if (capabilityProviders == null) {
            capabilityProviders = new ArrayList<>();
        }
        capabilityProviders.add(new CapabilityData<>(capability, provider, shouldApply));
    }

    void removeCapability(BlockCapability<?, ?> capability) {
        if (capabilityProviders != null) {
            capabilityProviders.removeIf(data -> data.capability() == capability);
        }
    }

    @Nullable
    public BlockEntityTicker<BE> getTicker(boolean isClient) {
        return isClient ? clientTicker : serverTicker;
    }

    void registerCapabilityProviders(RegisterCapabilitiesEvent event) {
        if (capabilityProviders != null) {
            for (CapabilityData<BE, ?, ?> capabilityProvider : capabilityProviders) {
                capabilityProvider.registerProvider(event, get());
            }
        }
    }

    private record CapabilityData<BE extends BlockEntity, CAP, CONTEXT>(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider,
                                                                        BooleanSupplier shouldApply) {

        private void registerProvider(RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
            if (shouldApply.getAsBoolean()) {
                event.registerBlockEntity(capability, type, provider);
            }
        }
    }
}