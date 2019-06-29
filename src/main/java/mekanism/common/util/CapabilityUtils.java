package mekanism.common.util;

import java.util.function.Consumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Contract;

public final class CapabilityUtils {

    @Contract("null, _, _ -> false; _, null, _ -> false")
    public static boolean hasCapability(ICapabilityProvider provider, Capability<?> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return false;
        }
        return provider.hasCapability(cap, side);
    }

    @Contract("null, _, _ -> null; _, null, _ -> null")
    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return null;
        }
        return provider.getCapability(cap, side);
    }

    public static <T> OptionalCapability withCapability(ICapabilityProvider provider, Capability<T> cap, EnumFacing side, Consumer<T> consumer) {
        return new OptionalCapability(provider).orElseWith(cap, side, consumer);
    }

    public static <T> OptionalCapability withCapability(boolean testResult, ICapabilityProvider provider, Capability<T> cap, EnumFacing side, Consumer<T> consumer) {
        return new OptionalCapability(provider).orElseWith(testResult, cap, side, consumer);
    }

    public static class OptionalCapability {

        private final ICapabilityProvider provider;

        OptionalCapability(ICapabilityProvider provider) {
            this.provider = provider;
        }

        /**
         * If this chain has not yet been satisfied, try to get the capability and consume it.
         *
         * @param capability the cap to check
         * @param side       side to give to the provider
         * @param consumer   function to use the capability
         * @param <T>        the capability type
         *
         * @return this instance if unsuccessful, otherwise a no-op OptionalCapability
         */
        public <T> OptionalCapability orElseWith(Capability<T> capability, EnumFacing side, Consumer<T> consumer) {
            T cap = this.provider.getCapability(capability, side);
            if (cap != null) {
                consumer.accept(cap);
                return NoOpOptionalCapability.INSTANCE;
            }
            return this;
        }

        /**
         * Like {@link #orElseWith(Capability, EnumFacing, Consumer)}, but skips if boolean testResult fails.
         *
         * @param testResult if this param is false, cap is not processed
         * @param capability the cap to check
         * @param side       side to give to the provider
         * @param consumer   function to use the capability
         * @param <T>        the capability type
         *
         * @return this instance if unsuccessful (or testResult is false), otherwise a no-op OptionalCapability
         */
        public <T> OptionalCapability orElseWith(boolean testResult, Capability<T> capability, EnumFacing side, Consumer<T> consumer) {
            if (!testResult) {
                return this;
            }
            return orElseWith(capability, side, consumer);
        }
    }

    /**
     * All method should be no-op version of super, return this instance when the action succeeded
     */
    public static class NoOpOptionalCapability extends OptionalCapability {

        public static final NoOpOptionalCapability INSTANCE = new NoOpOptionalCapability();

        private NoOpOptionalCapability() {
            super(null);
        }

        @Override
        public <T> OptionalCapability orElseWith(Capability<T> capability, EnumFacing side, Consumer<T> consumer) {
            return this;
        }

        @Override
        public <T> OptionalCapability orElseWith(boolean testResult, Capability<T> capability, EnumFacing side, Consumer<T> consumer) {
            return this;
        }
    }
}