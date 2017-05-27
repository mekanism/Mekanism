package ic2.api.upgrade;

/**
 * The possible properties an {@link IUpgradableBlock} can support
 *
 * @author Player
 */
public enum UpgradableProperty {
	/**
	 * A block that processes something, taking upgrade process time + energy demand into account
	 */
	Processing,
	/**
	 * A simple count based upgrade property, e.g. extra range
	 */
	Augmentable,
	/**
	 * A block that is sensitive to redstone state/changes
	 */
	RedstoneSensitive,
	/**
	 * A block that can have it's tier changed
	 */
	Transformer,
	/**
	 * A block that can have the amount of energy it stores changed
	 */
	EnergyStorage,
	/**
	 * A block that consumes items
	 */
	ItemConsuming,
	/**
	 * A block that produces items
	 */
	ItemProducing,
	/**
	 * A block that consumes fluids
	 */
	FluidConsuming,
	/**
	 * A block that produces fluids
	 */
	FluidProducing,
}