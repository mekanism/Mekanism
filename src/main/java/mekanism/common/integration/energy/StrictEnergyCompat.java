package mekanism.common.integration.energy;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.Capabilities.MultiTypeCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class StrictEnergyCompat implements IEnergyCompat {

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public MultiTypeCapability<IStrictEnergyHandler> getCapability() {
        return Capabilities.STRICT_ENERGY;
    }

    @Override
    public <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider) {
        return provider;
    }

    @Override
    public IStrictEnergyHandler wrapStrictEnergyHandler(IStrictEnergyHandler handler) {
        return handler;
    }

    @Nullable
    @Override
    public IStrictEnergyHandler getAsStrictEnergyHandler(Level level, BlockPos pos, @Nullable Direction context) {
        return getCapability().getCapability(level, pos, context);
    }

    @Override
    public CacheConverter<IStrictEnergyHandler> getCacheAndConverter(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
          Runnable invalidationListener) {
        return new CacheConverter<>(getCapability().createCache(level, pos, context, isValid, invalidationListener), Function.identity());
    }

    @Nullable
    @Override
    public IStrictEnergyHandler getStrictEnergyHandler(ItemStack stack) {
        return getCapability().getCapability(stack);
    }
}