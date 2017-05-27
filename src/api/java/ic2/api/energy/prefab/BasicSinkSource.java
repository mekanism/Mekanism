package ic2.api.energy.prefab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.ILocatable;

/**
 * Combination of BasicSink and BasicSource, see their respective documentation for details.
 *
 * <p>A subclass still has to implement acceptsEnergyFrom and emitsEnergyTo.
 */
public abstract class BasicSinkSource extends BasicEnergyTile implements IEnergySink, IEnergySource {
	public BasicSinkSource(TileEntity parent, double capacity, int sinkTier, int sourceTier) {
		super(parent, capacity);

		this.sinkTier = sinkTier;
		this.sourceTier = sourceTier;
		this.sourcePower = EnergyNet.instance.getPowerFromTier(sourceTier);

		if (getCapacity() < sourcePower) setCapacity(sourcePower);
	}

	public BasicSinkSource(ILocatable parent, double capacity, int sinkTier, int sourceTier) {
		super(parent, capacity);

		this.sinkTier = sinkTier;
		this.sourceTier = sourceTier;
		this.sourcePower = EnergyNet.instance.getPowerFromTier(sourceTier);

		if (getCapacity() < sourcePower) setCapacity(sourcePower);
	}

	public BasicSinkSource(World world, BlockPos pos, double capacity, int sinkTier, int sourceTier) {
		super(world, pos, capacity);

		this.sinkTier = sinkTier;
		this.sourceTier = sourceTier;
		this.sourcePower = EnergyNet.instance.getPowerFromTier(sourceTier);

		if (getCapacity() < sourcePower) setCapacity(sourcePower);
	}

	/**
	 * Set the IC2 energy tier for this sink.
	 *
	 * @param tier IC2 Tier.
	 */
	public void setSinkTier(int tier) {
		this.sinkTier = tier;
	}

	/**
	 * Set the IC2 energy tier for this source.
	 *
	 * @param tier IC2 Tier.
	 */
	public void setSourceTier(int tier) {
		double power = EnergyNet.instance.getPowerFromTier(tier);

		if (getCapacity() < power) setCapacity(power);

		this.sourceTier = tier;
		this.sourcePower = power;
	}

	// energy net interface >>

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
		return sinkTier;
	}

	@Override
	public double getOfferedEnergy() {
		return Math.min(energyStored, sourcePower);
	}

	@Override
	public void drawEnergy(double amount) {
		energyStored -= amount;
	}

	@Override
	public int getSourceTier() {
		return sourceTier;
	}

	// << energy net interface

	@Override
	protected String getNbtTagName() {
		return "IC2BasicSinkSource";
	}

	protected int sinkTier;
	protected int sourceTier;
	protected double sourcePower;
}
