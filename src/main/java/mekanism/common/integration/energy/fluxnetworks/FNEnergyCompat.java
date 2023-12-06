package mekanism.common.integration.energy.fluxnetworks;

import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities.MultiTypeCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@NothingNullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    @Override//TODO - 1.20.2: Test to make sure this doesn't cause a crash without FN (it probably will, if it doesn't make more return types provide the generic)
    public MultiTypeCapability<IFNEnergyStorage> getCapability() {
        return FNCapability.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return capabilityExists() && isConfigEnabled();
    }

    private boolean isConfigEnabled() {
        return EnergyUnit.FORGE_ENERGY.isEnabled() && !MekanismConfig.general.blacklistFluxNetworks.getOrDefault();
    }

    @Override
    public boolean capabilityExists() {
        return Mekanism.hooks.FluxNetworksLoaded;
    }

    @Override
    public <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, ?> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider) {
        return (obj, ctx) -> {
            IStrictEnergyHandler handler = provider.getCapability(obj, ctx);
            return handler != null && isConfigEnabled() ? wrapStrictEnergyHandler(handler) : null;
        };
    }

    @Override
    public Object wrapStrictEnergyHandler(IStrictEnergyHandler handler) {
        return new FNIntegration(handler);
    }

    @Nullable
    @Override
    public IStrictEnergyHandler getAsStrictEnergyHandler(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity tile, @Nullable Direction context) {
        IFNEnergyStorage capability = getCapability().getCapability(level, pos, state, tile, context);
        return capability == null ? null : new FNStrictEnergyHandler(capability);
    }

    @Override
    public CacheConverter<?> getCacheAndConverter(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
          Runnable invalidationListener) {
        return new CacheConverter<>(getCapability().createCache(level, pos, context, isValid, invalidationListener), FNStrictEnergyHandler::new);
    }

    @Nullable
    @Override
    public IStrictEnergyHandler getStrictEnergyHandler(ItemStack stack) {
        IFNEnergyStorage capability = getCapability().getCapability(stack);
        return capability == null ? null : new FNStrictEnergyHandler(capability);
    }
}