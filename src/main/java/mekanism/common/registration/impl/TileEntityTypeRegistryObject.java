package mekanism.common.registration.impl;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeRegistryObject<BE extends BlockEntity> extends MekanismDeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> {

    @Nullable
    private List<CapabilityData<BE, ?, ?>> capabilityProviders;
    @Nullable
    private BlockEntityTicker<BE> clientTicker;
    @Nullable
    private BlockEntityTicker<BE> serverTicker;

    public TileEntityTypeRegistryObject(ResourceKey<BlockEntityType<?>> key) {
        super(key);
    }

    @Nullable
    public BlockEntityTicker<BE> getTicker(boolean isClient) {
        return isClient ? clientTicker : serverTicker;
    }

    @Internal
    void tickers(@Nullable BlockEntityTicker<BE> clientTicker, @Nullable BlockEntityTicker<BE> serverTicker) {
        this.clientTicker = clientTicker;
        this.serverTicker = serverTicker;
    }

    @Internal
    void capabilities(@Nullable List<CapabilityData<BE, ?, ?>> capabilityProviders) {
        this.capabilityProviders = capabilityProviders;
    }

    @Internal
    void registerCapabilityProviders(RegisterCapabilitiesEvent event) {
        if (capabilityProviders != null) {
            for (CapabilityData<BE, ?, ?> capabilityProvider : capabilityProviders) {
                capabilityProvider.registerProvider(event, value());
            }
        }
    }

    record CapabilityData<BE extends BlockEntity, CAP, CONTEXT>(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider,
                                                                        BooleanSupplier shouldApply) {

        private void registerProvider(RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
            if (shouldApply.getAsBoolean()) {
                event.registerBlockEntity(capability, type, provider);
            }
        }
    }
}