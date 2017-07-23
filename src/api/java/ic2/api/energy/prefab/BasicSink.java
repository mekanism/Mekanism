package ic2.api.energy.prefab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;

/**
 * BasicSink is a simple delegate to provide an ic2 energy sink.
 *
 * <p>It's designed to be attached to a tile entity as a delegate. Functionally BasicSink acts as a
 * one-time configurable input energy buffer, thus providing a common use case for machines.
 *
 * <p>A simpler alternative is to use {@link BasicSinkTe.Sink}, which requires injecting into the
 * type hierarchy (using it as a super class). BasicSink however can be added to any class without
 * inheritance, but needs some forwards for keeping its state and registering with the energy net.
 *
 * <p>The constraints set by BasicSink like the strict tank-like energy buffering should provide a
 * more easy to use and stable interface than using IEnergySink directly while aiming for
 * optimal performance.
 *
 * <p>Using BasicSink involves the following steps:<ul>
 * <li>create a BasicSink instance in your TileEntity, typically in a field
 * <li>forward onLoad (or update), invalidate, onChunkUnload, readFromNBT and writeToNBT to the BasicSink
 *   instance.
 * <li>call useEnergy whenever appropriate. canUseEnergy determines if enough energy is available
 *   without consuming the energy.
 * <li>optionally use getEnergyStored to display the output buffer charge level
 * <li>optionally use setEnergyStored to sync the stored energy to the client (e.g. in the Container)</ul>
 *
 * <p>Example implementation code:
 * <pre><code>
 * public class SomeTileEntity extends TileEntity {
 *     // new basic energy sink, 1000 EU buffer, tier 1 (32 EU/t, LV)
 *     private BasicSink ic2EnergySink = new BasicSink(this, 1000, 1);
 *
 *     {@literal @}Override
 *     public void onLoad() {
 *         ic2EnergySink.onLoad(); // notify the energy sink
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void invalidate() {
 *         ic2EnergySink.invalidate(); // notify the energy sink
 *         ...
 *         super.invalidate(); // this is important for mc!
 *     }
 *
 *     {@literal @}Override
 *     public void onChunkUnload() {
 *         ic2EnergySink.onChunkUnload(); // notify the energy sink
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void readFromNBT(NBTTagCompound tag) {
 *         super.readFromNBT(tag);
 *
 *         ic2EnergySink.readFromNBT(tag);
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void writeToNBT(NBTTagCompound tag) {
 *         super.writeToNBT(tag);
 *
 *         ic2EnergySink.writeToNBT(tag);
 *         ...
 *     }
 *
 *     {@literal @}Override
 *     public void update() {
 *         if (ic2EnergySink.useEnergy(5)) { // use 5 eu from the sink's buffer this tick
 *             ... // do something with the energy
 *         }
 *         ...
 *     }
 *
 *     ...
 * }
 * </code></pre>
 */
public class BasicSink extends BasicEnergyTile implements IEnergySink {
	/**
	 * Constructor for a new BasicSink delegate.
	 *
	 * @param parent TileEntity represented by this energy sink.
	 * @param capacity Maximum amount of eu to store.
	 * @param tier IC2 tier, 1 = LV, 2 = MV, ...
	 */
	public BasicSink(TileEntity parent, double capacity, int tier) {
		super(parent, capacity);

		this.tier = tier;
	}

	public BasicSink(ILocatable parent, double capacity, int tier) {
		super(parent, capacity);

		this.tier = tier;
	}

	public BasicSink(World world, BlockPos pos, double capacity, int tier) {
		super(world, pos, capacity);

		this.tier = tier;
	}

	/**
	 * Set the IC2 energy tier for this sink.
	 *
	 * @param tier IC2 Tier.
	 */
	public void setSinkTier(int tier) {
		this.tier = tier;
	}

	// energy net interface >>

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
		return true;
	}

	@Override
	public double getDemandedEnergy() {
		return Math.max(0, capacity - energyStored);
	}

	@Override
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		energyStored += amount;

		return 0;
	}

	@Override
	public int getSinkTier() {
		return tier;
	}

	// << energy net interface

	@Override
	protected String getNbtTagName() {
		return "IC2BasicSink";
	}

	protected int tier;
}
