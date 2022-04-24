package mekanism.api.radiation;

import com.google.common.collect.Table;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * The RadiationManager handles radiation across all in-game dimensions. Radiation exposure levels are provided in _sieverts, defining a rate of accumulation of
 * equivalent dose. Get an instance from {@link mekanism.api.MekanismAPI#getRadiationManager()}.
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
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRadiationManager {

    /**
     * Helper to expose the ability to check if Mekanism's radiation system is enabled in the config.
     */
    boolean isRadiationEnabled();

    /**
     * Helper to access Mekanism's internal radiation damage source.
     *
     * @return Damage source used for radiation.
     */
    DamageSource getRadiationDamageSource();

    /**
     * Get the radiation level (in Sv/h) at a certain location.
     *
     * @param coord Location
     *
     * @return radiation level (in Sv).
     */
    double getRadiationLevel(Coord4D coord);

    /**
     * Get the radiation level (in Sv/h) at an entity's location. To get the radiation level of an entity use {@link
     * mekanism.api.radiation.capability.IRadiationEntity#getRadiation()}.
     *
     * @param entity - Entity to get the radiation level at.
     *
     * @return Radiation level (in Sv).
     */
    double getRadiationLevel(Entity entity);

    /**
     * Gets an unmodifiable table of the radiation sources tracked by this manager. This table keeps track of radiation sources on both a chunk and position based level.
     *
     * @return Unmodifiable table of radiation sources.
     */
    Table<Chunk3D, Coord4D, IRadiationSource> getRadiationSources();

    /**
     * Removes all radiation sources in a given chunk.
     *
     * @param chunk Chunk to clear radiation sources of.
     */
    void removeRadiationSources(Chunk3D chunk);

    /**
     * Removes the radiation source at the given location.
     *
     * @param coord Location.
     */
    void removeRadiationSource(Coord4D coord);

    /**
     * Applies a radiation source (Sv) of the given magnitude to a given location.
     *
     * @param coord     Location to release radiation.
     * @param magnitude Amount of radiation to apply (Sv).
     */
    void radiate(Coord4D coord, double magnitude);

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
     * Helper to "dump" any radioactive gases stored in the tanks handled by the given gas handler.
     *
     * @param coord            Location to dump radiation at.
     * @param gasHandler       Gas handler to process the tanks of.
     * @param clearRadioactive {@code true} to clear any gas tanks that have radioactive substances.
     *
     * @throws RuntimeException if {@code clearRadioactive = true} and the passed in handler does not expect to have {@link IGasHandler#setChemicalInTank(int,
     *                          ChemicalStack)} called wth an empty stack.
     */
    void dumpRadiation(Coord4D coord, IGasHandler gasHandler, boolean clearRadioactive);

    /**
     * Helper to "dump" any radioactive gases stored in the given gas tanks.
     *
     * @param coord            Location to dump radiation at.
     * @param gasTanks         Tanks to process.
     * @param clearRadioactive {@code true} to clear any gas tanks that have radioactive substances.
     */
    void dumpRadiation(Coord4D coord, List<IGasTank> gasTanks, boolean clearRadioactive);

    /**
     * Checks if the given {@link GasStack} is radioactive and if it is dumps a proportionate amount of radiation at the given location.
     *
     * @param coord Location to dump radiation at.
     * @param stack Stack to check.
     *
     * @return {@code true} if the stack was radioactive and radiation got dumped.
     *
     * @apiNote If radiation is disabled this may still return {@code true}.
     */
    boolean dumpRadiation(Coord4D coord, GasStack stack);
}