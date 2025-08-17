package mekanism.api.radiation;

import com.google.common.collect.Table;
import java.util.Collections;
import java.util.List;
import mekanism.api.Chunk3D;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * The RadiationManager handles radiation across all in-game dimensions. Radiation exposure levels are provided in _sieverts, defining a rate of accumulation of
 * equivalent dose.
 *
 * <br><br>
 * For reference, here are examples of equivalent dose (credit: wikipedia)
 * <ul>
 * <li>100 nSv: baseline dose (banana equivalent dose)</li>
 * <li>250 nSv: airport security screening</li>
 * <li>1 mSv: annual total civilian dose equivalent</li>
 * <li>50 mSv: annual total occupational equivalent dose limit</li>
 * <li>250 mSv: total dose equivalent from 6-month trip to mars</li>
 * <li>1 Sv: maximum allowed dose allowed for NASA astronauts over their careers</li>
 * <li>5 Sv: dose required to (50% chance) kill human if received over 30-day period</li>
 * <li>50 Sv: dose received after spending 10 min next to Chernobyl reactor core directly after meltdown</li>
 * </ul>
 * For defining rate of accumulation, we use _sieverts per hour_ (Sv/h). Here are examples of dose accumulation rates.
 * <ul>
 * <li>100 nSv/h: max recommended human irradiation</li>
 * <li>2.7 uSv/h: irradiation from airline at cruise altitude</li>
 * <li>190 mSv/h: highest reading from fallout of Trinity (Manhattan project test) bomb, _20 miles away_, 3 hours after detonation</li>
 * <li>~500 Sv/h: irradiation inside primary containment vessel of Fukushima power station (at this rate, it takes 30 seconds to accumulate a median lethal dose)</li>
 * </ul>
 *
 * @see IRadiationManager#INSTANCE
 */
@NothingNullByDefault
public interface IRadiationManager {

    /**
     * Provides access to Mekanism's implementation of {@link IRadiationManager}.
     *
     * @since 10.4.0
     */
    IRadiationManager INSTANCE = MekanismAPI.getService(IRadiationManager.class);

    /**
     * Helper to expose the ability to check if Mekanism's radiation system is enabled in the config.
     */
    boolean isRadiationEnabled();

    /**
     * {@return the baseline radiation level (inclusive) of the world and all things in it}
     *
     * @implNote 100 nSv/h
     * @since 10.7.11
     */
    double baselineRadiation();

    /**
     * {@return the minimum radiation level that has a noticeable effect on entities}
     *
     * @implNote 10 uSv/h
     * @since 10.7.11
     */
    double minRadiationMagnitude();

    /**
     * Helper to access Mekanism's internal radiation damage source.
     *
     * @param registryAccess Registry access to create the damage source with.
     *
     * @return Damage source used for radiation.
     */
    DamageSource getRadiationDamageSource(RegistryAccess registryAccess);

    /**
     * Helper to access Mekanism's internal radiation damage type's resource key.
     *
     * @return Resource key of the damage type used for radiation.
     *
     * @since 10.4.0
     */
    ResourceKey<DamageType> getRadiationDamageTypeKey();

    /**
     * Get the radiation level (in Sv/h) at a certain location.
     *
     * @param level the level to check
     * @param pos   Location
     *
     * @return radiation level (in Sv/h).
     *
     * @since 10.7.15
     */
    double getRadiationLevel(Level level, BlockPos pos);

    /**
     * Get the radiation level (in Sv/h) at a certain location.
     *
     * @param pos Location
     *
     * @return radiation level (in Sv/h).
     *
     * @deprecated Replace with {@link #getRadiationLevel(Level, BlockPos)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default double getRadiationLevel(GlobalPos pos) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            return getRadiationLevel(level, pos.pos());
        }
        return baselineRadiation();
    }

    /**
     * Get the radiation level (in Sv/h) at an entity's location. To get the radiation level of an entity use
     * {@link mekanism.api.radiation.capability.IRadiationEntity#getRadiation()}.
     *
     * @param entity - Entity to get the radiation level at.
     *
     * @return Radiation level (in Sv/h).
     */
    double getRadiationLevel(Entity entity);

    /**
     * Generates a table of the radiation sources tracked by this manager. This table keeps track of radiation sources on both a chunk and position based level.
     *
     * @return Generated table of radiation sources.
     *
     * @deprecated Replace with {@link #getRadiationSources(Level, int, int)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    Table<Chunk3D, GlobalPos, IRadiationSource> getRadiationSources();

    /**
     * Gets a list of the radiation sources with a source point in the chunk. Minimise calling in hot paths.
     *
     * @param level  The level to check
     * @param chunkX The X position of the Chunk
     * @param chunkZ The Z position of the Chunk
     *
     * @return A new list containing the relevant sources, or {@link Collections#emptyList()} when there are none.
     *
     * @since 10.7.15
     */
    List<IRadiationSource> getRadiationSources(Level level, int chunkX, int chunkZ);

    /**
     * Removes all radiation sources in a given chunk.
     *
     * @since 10.7.15
     */
    void removeRadiationSources(Level level, int chunkX, int chunkZ);

    /**
     * Removes all radiation sources in a given chunk.
     *
     * @deprecated Replace with {@link #removeRadiationSources(Level, int, int)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default void removeRadiationSources(Chunk3D chunk) {
        Level level = backCompat$getLevel(chunk.dimension);
        if (level != null) {
            removeRadiationSources(level, chunk.x, chunk.z);
        }
    }

    /**
     * Removes the radiation source at the given location.
     *
     * @param level The level on which to act
     * @param pos   The location on which to act
     *
     * @since 10.7.15
     */
    void removeRadiationSource(Level level, BlockPos pos);

    /**
     * Removes the radiation source at the given location.
     *
     * @param pos Location.
     *
     * @deprecated Replace with {@link #removeRadiationSource(Level, BlockPos)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default void removeRadiationSource(GlobalPos pos) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            removeRadiationSource(level, pos.pos());
        }
    }

    /**
     * Applies a radiation source (Sv) of the given magnitude to a given location.
     *
     * @param level     The level on which to act
     * @param pos       Location to release radiation.
     * @param magnitude Amount of radiation to apply (Sv).
     *
     * @since 10.7.15
     */
    void radiate(Level level, BlockPos pos, double magnitude);

    /**
     * Applies a radiation source (Sv) of the given magnitude to a given location.
     *
     * @param pos       Location to release radiation.
     * @param magnitude Amount of radiation to apply (Sv).
     *
     * @deprecated Replace with {@link #radiate(Level, BlockPos, double)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default void radiate(GlobalPos pos, double magnitude) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            radiate(level, pos.pos(), magnitude);
        }
    }

    /**
     * Applies an additional magnitude of radiation (Sv) to the given entity after taking into account the radiation resistance provided to the entity by its armor.
     *
     * @param entity    The entity to radiate.
     * @param magnitude Dosage of radiation to apply before radiation resistance (Sv).
     *
     * @implNote This method does not add any radiation to players in creative or spectator.
     */
    void radiate(LivingEntity entity, double magnitude);

    /**
     * Helper to "dump" any radioactive chemicals stored in the tanks handled by the given chemical handler.
     *
     * @param level            The level on which to act
     * @param pos              Location to dump radiation at.
     * @param chemicalHandler  Chemical handler to process the tanks of.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     *
     * @throws RuntimeException if {@code clearRadioactive = true} and the passed in handler does not expect to have
     *                          {@link IChemicalHandler#setChemicalInTank(int, ChemicalStack)} called wth an empty stack.
     * @since 10.7.15
     */
    void dumpRadiation(Level level, BlockPos pos, IChemicalHandler chemicalHandler, boolean clearRadioactive);

    /**
     * Helper to "dump" any radioactive chemicals stored in the tanks handled by the given chemical handler.
     *
     * @param pos              Location to dump radiation at.
     * @param chemicalHandler  Chemical handler to process the tanks of.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     *
     * @throws RuntimeException if {@code clearRadioactive = true} and the passed in handler does not expect to have
     *                          {@link IChemicalHandler#setChemicalInTank(int, ChemicalStack)} called wth an empty stack.
     * @deprecated Replace with {@link #dumpRadiation(Level, BlockPos, IChemicalHandler, boolean)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default void dumpRadiation(GlobalPos pos, IChemicalHandler chemicalHandler, boolean clearRadioactive) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            dumpRadiation(level, pos.pos(), chemicalHandler, clearRadioactive);
        }
    }

    /**
     * Helper to "dump" any radioactive chemicals stored in the given chemical tanks.
     *
     * @param level            The level on which to act
     * @param pos              Location to dump radiation at.
     * @param chemicalTanks    Tanks to process.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     *
     * @since 10.7.15
     */
    void dumpRadiation(Level level, BlockPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive);

    /**
     * Helper to "dump" any radioactive chemicals stored in the given chemical tanks.
     *
     * @param pos              Location to dump radiation at.
     * @param chemicalTanks    Tanks to process.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     *
     * @deprecated Replace with {@link #dumpRadiation(Level, BlockPos, List, boolean)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default void dumpRadiation(GlobalPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            dumpRadiation(level, pos.pos(), chemicalTanks, clearRadioactive);
        }
    }

    /**
     * Checks if the given {@link ChemicalStack} is radioactive and if it is dumps a proportionate amount of radiation at the given location.
     *
     * @param level The level on which to act
     * @param pos   Location to dump radiation at.
     * @param stack Stack to check.
     *
     * @return {@code true} if the stack was radioactive and radiation got dumped.
     *
     * @apiNote If radiation is disabled this may still return {@code true}.
     * @since 10.7.15
     */
    boolean dumpRadiation(Level level, BlockPos pos, ChemicalStack stack);

    /**
     * Checks if the given {@link ChemicalStack} is radioactive and if it is dumps a proportionate amount of radiation at the given location.
     *
     * @param pos   Location to dump radiation at.
     * @param stack Stack to check.
     *
     * @return {@code true} if the stack was radioactive and radiation got dumped.
     *
     * @apiNote If radiation is disabled this may still return {@code true}.
     * @deprecated Replace with {@link #dumpRadiation(Level, BlockPos, ChemicalStack)}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    default boolean dumpRadiation(GlobalPos pos, ChemicalStack stack) {
        Level level = backCompat$getLevel(pos.dimension());
        if (level != null) {
            return dumpRadiation(level, pos.pos(), stack);
        }
        return false;
    }

    @Internal
    @Nullable
    private static Level backCompat$getLevel(ResourceKey<Level> dimension) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            return null;
        }
        return currentServer.getLevel(dimension);
    }
}