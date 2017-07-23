package ic2.api.reactor;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import ic2.api.info.ILocatable;

/**
 * Interface implemented nuclear reactors, expected to be a {@link TileEntity}, but does not have to be.
 */
public interface IReactor extends ILocatable {
	/**
	 * Return the tile entity representing the reactor core.
	 * <br/>
	 * If null, {@link #getReactorWorld()} and {@link #getReactorPos()} can still be used if it's world and/or position are desired.
	 *
	 * @return the reactor core's TE or null.
	 */
	public TileEntity getCoreTe();

	/**
	 * Get the reactor's heat.
	 *
	 * @return The reactor's heat
	 */
	public int getHeat();

	/**
	 * Set the reactor's heat.
	 *
	 * @param heat reactor heat
	 */
	public void setHeat(int heat);

	/**
	 * Increase the reactor's heat.
	 *
	 * Use negative values to decrease.
	 *
	 * @param amount amount of heat to add
	 * @return The reactor's heat after adding the specified amount
	 */
	public int addHeat(int amount);

	/**
	 * Get the reactor's maximum heat before exploding.
	 *
	 * @return Maximum heat value
	 */
	public int getMaxHeat();

	/**
	 * Set the reactor's stored maxHeat variable.
	 * Used by plating to increase the reactors MaxHeat capacity.
	 * Needs to be called during each cycle process.
	 */
	public void setMaxHeat(int newMaxHeat);

	/**
	 * add Heat to a EmitHeat Buffer
	 * for use in  Reactor operation.. need to be use
	 * for all Componetents with self-cooling
	 * no more magic heat disappear
	 */


	public void addEmitHeat(int heat);

	/**
	 * Get's the reactor's HEM (Heat Effect Modifier)
	 * Basic value is 1.0F.
	 * Reducing the value causes a weakening/reduction of the heat-based sideeffects of reactors
	 * (F.e. water evaporation, melting, damaging entitys, etc)
	 *
	 * @return HEM
	 */
	public float getHeatEffectModifier();

	/**
	 * Set's the reactor's HEM
	 * Needs to be called during each cycle process.
	 */
	public void setHeatEffectModifier(float newHEM);

	/**
	 * Get the reactor's energy output.
	 *
	 * @return Energy output, not multiplied by the base EU/t value
	 */
	public float getReactorEnergyOutput();

	/**
	 * Get the reactor's energy output.
	 *
	 * @return Energy output, multiplied with base EU/t value and MainConfig.get().getFloat("balance/energy/generator/nuclear")
	 */
	public double getReactorEUEnergyOutput();

	/**
	 * Add's the given amount of energy to the Reactor's output.
	 *
	 * @return Energy output after adding the value, not multiplied by the base EU/t value
	 */
	public float addOutput(float energy);

	/**
	 * Get the item at the specified grid coordinates.
	 *
	 * @param x X position of the item, out of bounds returns null
	 * @param y Y position of the item, out of bounds returns null
	 * @return The item or null if there is no item
	 */
	public ItemStack getItemAt(int x, int y);

	/**
	 * Set the item at the specified grid coordinates.
	 *
	 * @param x X position of the item, out of bounds is a no-op
	 * @param y Y position of the item, out of bounds is a no-op
	 * @param item The item to set.
	 */
	public void setItemAt(int x, int y, ItemStack item);

	/**
	 * Explode the reactor.
	 */
	public void explode();

	/**
	 * Get the reactor's tick rate (game ticks per reactor tick).
	 *
	 * @return Tick rate
	 */
	public int getTickRate();

	/**
	 * Get whether the reactor is active and supposed to produce energy
	 * @return Whether the reactor is active
	 */
	public boolean produceEnergy();

	// TODO: add the rs modifier capability to the api somehow

	/**
	 * @return true if  the reactor is FluidCooled
	 */
	public boolean isFluidCooled();
}
