package mekanism.common.capabilities.resolver;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@NothingNullByDefault
public interface ICapabilityResolver<CONTEXT> {

    /**
     * Gets the list of capabilities this resolver is able to resolve.
     *
     * @return List of capabilities this resolver can resolve.
     */
    List<BlockCapability<?, CONTEXT>> getSupportedCapabilities();

    /**
     * Resolves a given capability from a given side. This value should be cached for later invalidation, as well as quicker re-lookup.
     *
     * @param capability Capability
     * @param side       Side
     *
     * @return LazyOptional for the given capability
     *
     * @apiNote This method should only be called with capabilities that are in {@link #getSupportedCapabilities()}
     * @implNote The result should be cached
     */
    @Nullable
    <T> T resolve(BlockCapability<T, CONTEXT> capability, @UnknownNullability CONTEXT side);

    /**
     * Invalidates the given capability on the given side.
     *
     * @param capability Capability
     * @param context    Context
     */
    void invalidate(BlockCapability<?, CONTEXT> capability, @UnknownNullability CONTEXT context);

    /**
     * Invalidates all cached capabilities.
     */
    void invalidateAll();
}