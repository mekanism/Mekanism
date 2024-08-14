package mekanism.api.radiation;

import com.google.common.collect.Table;
import java.util.List;
import java.util.ServiceLoader;
import mekanism.api.Chunk3D;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
    IRadiationManager INSTANCE = ServiceLoader.load(IRadiationManager.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IRadiationManager found"));

    /**
     * Helper to expose the ability to check if Mekanism's radiation system is enabled in the config.
     */
    boolean isRadiationEnabled();

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
     * @param pos Location
     *
     * @return radiation level (in Sv/h).
     */
    double getRadiationLevel(GlobalPos pos);

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
     * Gets an unmodifiable table of the radiation sources tracked by this manager. This table keeps track of radiation sources on both a chunk and position based level.
     *
     * @return Unmodifiable table of radiation sources.
     */
    Table<Chunk3D, GlobalPos, IRadiationSource> getRadiationSources();

    /**
     * Removes all radiation sources in a given chunk.
     *
     * @param chunk Chunk to clear radiation sources of.
     */
    void removeRadiationSources(Chunk3D chunk);

    /**
     * Removes the radiation source at the given location.
     *
     * @param pos Location.
     */
    void removeRadiationSource(GlobalPos pos);

    /**
     * Applies a radiation source (Sv) of the given magnitude to a given location.
     *
     * @param pos       Location to release radiation.
     * @param magnitude Amount of radiation to apply (Sv).
     */
    void radiate(GlobalPos pos, double magnitude);

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
     * Helper to "dump" any radioactive chemicals stored in the tanks handled by the given gas handler.
     *
     * @param pos              Location to dump radiation at.
     * @param chemicalHandler  Chemical handler to process the tanks of.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     *
     * @throws RuntimeException if {@code clearRadioactive = true} and the passed in handler does not expect to have
     *                          {@link IChemicalHandler#setChemicalInTank(int, ChemicalStack)} called wth an empty stack.
     */
    void dumpRadiation(GlobalPos pos, IChemicalHandler chemicalHandler, boolean clearRadioactive);

    /**
     * Helper to "dump" any radioactive chemicals stored in the given gas tanks.
     *
     * @param pos              Location to dump radiation at.
     * @param chemicalTanks    Tanks to process.
     * @param clearRadioactive {@code true} to clear any chemical tanks that have radioactive substances.
     */
    void dumpRadiation(GlobalPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive);

    /**
     * Checks if the given {@link ChemicalStack} is radioactive and if it is dumps a proportionate amount of radiation at the given location.
     *
     * @param pos   Location to dump radiation at.
     * @param stack Stack to check.
     *
     * @return {@code true} if the stack was radioactive and radiation got dumped.
     *
     * @apiNote If radiation is disabled this may still return {@code true}.
     */
    boolean dumpRadiation(GlobalPos pos, ChemicalStack stack);
}