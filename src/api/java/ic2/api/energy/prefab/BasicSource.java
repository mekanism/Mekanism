package ic2.api.energy.prefab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.ILocatable;

/**
 * BasicSource is a simple delegate to add an ic2 energy source.
 *
 * <p>It's designed to be attached to a tile entity as a delegate. Functionally BasicSource acts as a
 * one-time configurable output energy buffer, thus providing a common use case for generators.
 *
 * <p>A simpler alternative is to use {@link BasicSinkTe.Source}, which requires injecting into the
 * type hierarchy (using it as a super class). BasicSource however can be added to any class without
 * inheritance, but needs some forwards for keeping its state and registering with the energy net.
 *
 * <p>The constraints set by BasicSource like the strict tank-like energy buffering should provide a
 * more easy to use and stable interface than using IEnergySource directly while aiming for
 * optimal performance.
 *
 * <p>Using BasicSource involves the following steps:<ul>
 * <li>create a BasicSource instance in your TileEntity, typically in a field
 * <li>forward onLoad (or update), invalidate, onChunkUnload, readFromNBT ans writeToNBT to the BasicSource
 *   instance.
 * <li>call addEnergy whenever appropriate, using getFreeCapacity may determine if e.g. the generator
 *   should run
 * <li>optionally use getEnergyStored to display the output buffer charge level
 * <li>optionally use setEnergyStored to sync the stored energy to the client (e.g. in the Container)</ul>
 *
 * <p>Example implementation code:
 * <pre><code>
 * public class SomeTileEntity extends TileEntity {
 *     // new basic energy source, 1000 EU buffer, tier 1 (32 EU/t, LV)
 *     private BasicSource ic2EnergySource = new BasicSource(this, 1000, 1);
 *
 *     {@literal @}Override
 *     public void onLoad() {
 *         ic2EnergySource.onLoad(); // notify the energy source
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void invalidate() {
 *         ic2EnergySource.invalidate(); // notify the energy source
 *         ...
 *         super.invalidate(); // this is important for mc!
 *     }
 *
 *     {@literal @}Override
 *     public void onChunkUnload() {
 *         ic2EnergySource.onChunkUnload(); // notify the energy source
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void readFromNBT(NBTTagCompound tag) {
 *         super.readFromNBT(tag);
 *
 *         ic2EnergySource.readFromNBT(tag);
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void writeToNBT(NBTTagCompound tag) {
 *         super.writeToNBT(tag);
 *
 *         ic2EnergySource.writeToNBT(tag);
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void update() {
 *         ic2EnergySource.addEnergy(5); // add 5 eu to the source's buffer this tick
 *         ...
 *     }
 *
 *     ...
 * }
 * </code></pre>
 */
public class BasicSource extends BasicEnergyTile implements IEnergySource {
	/**
	 * Constructor for a new BasicSource delegate.
	 *
	 * @param parent1 Base TileEntity represented by this energy source.
	 * @param capacity1 Maximum amount of eu to store.
	 * @param tier1 IC2 tier, 1 = LV, 2 = MV, ...
	 */
	public BasicSource(TileEntity parent, double capacity, int tier) {
		super(parent, capacity);

		this.tier = tier;
		this.power = EnergyNet.instance.getPowerFromTier(tier);

		if (getCapacity() < power) setCapacity(power);
	}

	public BasicSource(ILocatable parent, double capacity, int tier) {
		super(parent, capacity);

		this.tier = tier;
		this.power = EnergyNet.instance.getPowerFromTier(tier);

		if (getCapacity() < power) setCapacity(power);
	}

	public BasicSource(World world, BlockPos pos, double capacity, int tier) {
		super(world, pos, capacity);

		this.tier = tier;
		this.power = EnergyNet.instance.getPowerFromTier(tier);

		if (getCapacity() < power) setCapacity(power);
	}

	/**
	 * Set the IC2 energy tier for this source.
	 *
	 * @param tier1 IC2 Tier.
	 */
	public void setSourceTier(int tier) {
		double power = EnergyNet.instance.getPowerFromTier(tier);

		if (getCapacity() < power) setCapacity(power);

		this.tier = tier;
		this.power = power;
	}

	// energy net interface >>

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
		return true;
	}

	@Override
	public double getOfferedEnergy() {
		return Math.min(energyStored, power);
	}

	@Override
	public void drawEnergy(double amount) {
		energyStored -= amount;
	}

	@Override
	public int getSourceTier() {
		return tier;
	}

	// << energy net interface

	@Override
	protected String getNbtTagName() {
		return "IC2BasicSource";
	}

	protected int tier;
	protected double power;
}
