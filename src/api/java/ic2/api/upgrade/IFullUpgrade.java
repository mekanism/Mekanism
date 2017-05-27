package ic2.api.upgrade;

/**
 * An interface to mark an item supporting all {@link UpgradableProperty} type upgrades
 *
 * @author Player, Chocohead
 */
public interface IFullUpgrade extends IAugmentationUpgrade, IEnergyStorageUpgrade, IFluidConsumingUpgrade,
		IFluidProducingUpgrade, IItemConsumingUpgrade, IItemProducingUpgrade, IProcessingUpgrade,
		IRedstoneSensitiveUpgrade, ITransformerUpgrade {

}