package mekanism.common.component.containers.energy;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeEnergyStorage;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.component.UpgradeAware.UpgradeAmount;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class EnergyContainerBuilder {

    public static final IContainerCreator<IEnergyContainer, Long> ENERGY_CUBE = creator(ComponentBackedEnergyCubeContainer::create);
    public static final IContainerCreator<IEnergyContainer, Long> RESISTIVE_HEATER = creator(ComponentBackedResistiveEnergyContainer::create);
    public static final IContainerCreator<IEnergyContainer, Long> DIGITAL_MINER = creator(attachedAccess -> new ComponentBackedEnergyContainer(
          attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), new LazyMinerStorage(attachedAccess))
    );
    public static final IContainerCreator<IEnergyContainer, Long> DIMENSIONAL_STABILIZER = creator(attachedAccess -> new ComponentBackedEnergyContainer(
          attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), new LazyStabilizerStorage(attachedAccess))
    );
    public static final IContainerCreator<IEnergyContainer, Long> ELECTROLYTIC_SEPARATOR = creator(attachedAccess -> new ComponentBackedEnergyContainer(
          attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), new LazySeparatorStorage(attachedAccess))
    );

    private EnergyContainerBuilder() {
    }

    public static IContainerCreator<IEnergyContainer, Long> manualCreator(LongSupplier maxEnergy) {
        return manualCreator(() -> MekanismUtils.calculateUsage(maxEnergy.getAsLong()), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> manualCreator(IntSupplier rate, LongSupplier maxEnergy) {
        return basicCreator(BasicEnergyContainer.manualOnly, BasicEnergyContainer.manualOnly, rate, maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(LongSupplier maxEnergy) {
        return basicCreator(() -> MekanismUtils.calculateUsage(maxEnergy.getAsLong()), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> mekaGear(DoubleSupplier percentageRate, LongSupplier maxEnergy) {
        return creator(attachedAccess -> {
            LongSupplier scaledCapacity = () -> ModuleEnergyUnit.getEnergyCapacity(attachedAccess, maxEnergy);
            return new ComponentBackedEnergyContainer(
                  attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
                  scaledCapacity, EnergyUtils.calculateRate(percentageRate, scaledCapacity)
            );
        });
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(DoubleSupplier percentageRate, LongSupplier maxEnergy) {
        return basicCreator(EnergyUtils.calculateRate(percentageRate, maxEnergy), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(IntSupplier rate, LongSupplier maxEnergy) {
        return basicCreator(BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), rate, maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(BlockType type) {
        LongSupplier maxEnergy = type.get(AttributeEnergyStorage.class);
        if (maxEnergy == null) {
            AttributeEnergy attributeEnergy = type.getOrThrow(AttributeEnergy.class);
            return basicCreator(attributeEnergy::getStorage);
        }
        return basicCreator(maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> upgradeableUsageStorageCreator(BlockType type, boolean exponential) {
        AttributeEnergy attributeEnergy = type.getOrThrow(AttributeEnergy.class);
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(
              attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), new LazyUsageStorage(attachedAccess, attributeEnergy, exponential))
        );
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, DoubleSupplier percentageRate,
          LongSupplier maxEnergy) {
        return basicCreator(canExtract, canInsert, EnergyUtils.calculateRate(percentageRate, maxEnergy), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, IntSupplier rate,
          LongSupplier maxEnergy) {
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(attachedAccess, canExtract, canInsert, maxEnergy, rate));
    }

    public static IContainerCreator<IEnergyContainer, Long> creator(Function<ItemAccess, IEnergyContainer> creator) {
        return new BaseEnergyContainerCreator(creator);
    }

    private record BaseEnergyContainerCreator(Function<ItemAccess, IEnergyContainer> creator) implements IContainerCreator<IEnergyContainer, Long> {

        @Override
        public int totalContainers() {
            return 1;
        }

        @Override
        public Long initStorage() {
            return 0L;
        }

        @Override
        public IEnergyContainer create(ItemAccess attachedAccess, int containerIndex) {
            return creator.apply(attachedAccess);
        }
    }

    private static class LazyUsageStorage implements LongSupplier {

        protected final AttributeEnergy attributeEnergy;
        protected final ItemAccess attachedAccess;
        protected final boolean exponential;
        /// @implNote Only null until this supplier is first resolved
        @Nullable
        protected UpgradeAmount lastInstalledEnergy;
        /// @implNote Only null until this supplier is first resolved
        @Nullable
        protected UpgradeAmount lastInstalledSpeed;
        protected long value;

        private LazyUsageStorage(ItemAccess attachedAccess, AttributeEnergy attributeEnergy, boolean exponential) {
            this.attachedAccess = attachedAccess;
            this.attributeEnergy = attributeEnergy;
            this.exponential = exponential;
        }

        protected boolean checkOutdated(ItemResource resource) {
            return false;
        }

        @Override
        public long getAsLong() {
            ItemResource resource = attachedAccess.getResource();
            UpgradeAware upgradeAware = resource.getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            UpgradeAmount installedSpeed = upgradeAware.getUpgradeCount(UpgradeIds.SPEED);
            UpgradeAmount installedEnergy = upgradeAware.getUpgradeCount(UpgradeIds.ENERGY);
            if (checkOutdated(resource) || !installedSpeed.equals(lastInstalledSpeed) || !installedEnergy.equals(lastInstalledEnergy)) {
                lastInstalledSpeed = installedSpeed;
                lastInstalledEnergy = installedEnergy;
                int energyPerTick = getEnergyPerTick(lastInstalledSpeed, lastInstalledEnergy);
                int ticksRequired;
                if (exponential) {
                    ticksRequired = 1;
                } else {
                    ticksRequired = Math.max(1, MathUtils.clampToInt(
                          attributeEnergy.getBaseTicksRequired() * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -lastInstalledSpeed.fraction())
                    ));
                }
                value = energyPerTick * attributeEnergy.getMultiplier(ticksRequired);
            }
            return value;
        }

        protected int getEnergyPerTick(UpgradeAmount installedSpeed, UpgradeAmount installedEnergy) {
            int baseEnergyPerTick = attributeEnergy.getUsage();
            int energyPerTick = Mth.ceil(baseEnergyPerTick * Math.pow(
                  MekanismConfig.general.maxUpgradeMultiplier.get(),
                  2 * installedSpeed.fraction() - installedEnergy.fraction()
            ));
            if (exponential) {
                return energyPerTick * (1 + installedSpeed.stored());
            }
            return energyPerTick;
        }
    }

    private static class LazyMinerStorage extends LazyUsageStorage {

        private boolean wasSilkTouch;
        private int lastRadius;
        private int lastMinY;
        private int lastMaxY;

        private LazyMinerStorage(ItemAccess attachedAccess) {
            super(attachedAccess, MekanismBlockTypes.DIGITAL_MINER.getOrThrow(AttributeEnergy.class), false);
            ItemResource resource = attachedAccess.getResource();
            wasSilkTouch = resource.getOrDefault(MekanismDataComponents.SILK_TOUCH, false);
            lastRadius = resource.getOrDefault(MekanismDataComponents.RADIUS, TileEntityDigitalMiner.DEFAULT_RADIUS);
            lastMinY = resource.getOrDefault(MekanismDataComponents.MIN_Y, 0);
            lastMaxY = resource.getOrDefault(MekanismDataComponents.MAX_Y, TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE);
        }

        @Override
        protected boolean checkOutdated(ItemResource resource) {
            boolean silkTouch = resource.getOrDefault(MekanismDataComponents.SILK_TOUCH, false);
            int radius = resource.getOrDefault(MekanismDataComponents.RADIUS, TileEntityDigitalMiner.DEFAULT_RADIUS);
            int minY = resource.getOrDefault(MekanismDataComponents.MIN_Y, 0);
            int maxY = resource.getOrDefault(MekanismDataComponents.MAX_Y, TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE);
            if (silkTouch != wasSilkTouch || radius != lastRadius || minY != lastMinY || maxY != lastMaxY) {
                wasSilkTouch = silkTouch;
                lastRadius = radius;
                lastMinY = minY;
                lastMaxY = maxY;
                return true;
            }
            return false;
        }

        @Override
        protected int getEnergyPerTick(UpgradeAmount installedSpeed, UpgradeAmount installedEnergy) {
            int energyPerTick = super.getEnergyPerTick(installedSpeed, installedEnergy);
            return MinerEnergyContainer.calculateEnergyPerTick(energyPerTick, null, wasSilkTouch, lastRadius, lastMinY, lastMaxY);
        }
    }

    private static class LazySeparatorStorage extends LazyUsageStorage {

        private boolean wasMakingHydrogen;

        private LazySeparatorStorage(ItemAccess attachedAccess) {
            super(attachedAccess, MekanismBlockTypes.ELECTROLYTIC_SEPARATOR.getOrThrow(AttributeEnergy.class), true);
        }

        @Override
        public long getAsLong() {
            boolean isMakingHydrogen = ContainerType.CHEMICAL.createContainer(attachedAccess, 0).resource().is(ChemicalIds.HYDROGEN) ||
                                       ContainerType.CHEMICAL.createContainer(attachedAccess, 1).resource().is(ChemicalIds.HYDROGEN);
            UpgradeAware upgradeAware = attachedAccess.getResource().getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            UpgradeAmount installedSpeed = upgradeAware.getUpgradeCount(UpgradeIds.SPEED);
            boolean outDated = false;
            if (wasMakingHydrogen != isMakingHydrogen || !installedSpeed.equals(lastInstalledSpeed)) {
                wasMakingHydrogen = isMakingHydrogen;
                lastInstalledSpeed = installedSpeed;
                if (wasMakingHydrogen) {
                    //Note: getStorage includes the base multiplier
                    value = attributeEnergy.getStorage() * (int) Math.pow(2, lastInstalledSpeed.stored());
                    return value;
                }
                outDated = true;
            }
            UpgradeAmount installedEnergy = upgradeAware.getUpgradeCount(UpgradeIds.ENERGY);
            if (outDated || !installedEnergy.equals(lastInstalledEnergy)) {
                lastInstalledEnergy = installedEnergy;
                value = getEnergyPerTick(lastInstalledSpeed, lastInstalledEnergy) * attributeEnergy.getBaseMultiplier();
            }
            return value;
        }
    }

    private static class LazyStabilizerStorage implements LongSupplier {

        protected final AttributeEnergy attributeEnergy;
        protected final ItemAccess attachedAccess;
        private final int maxEnergyPerTick;
        /// @implNote Only null until this supplier is first resolved
        @Nullable
        protected UpgradeAmount lastInstalledEnergy;
        protected long value;

        private LazyStabilizerStorage(ItemAccess attachedAccess) {
            this.attachedAccess = attachedAccess;
            this.attributeEnergy = MekanismBlockTypes.DIMENSIONAL_STABILIZER.getOrThrow(AttributeEnergy.class);
            this.maxEnergyPerTick = this.attributeEnergy.getUsage() * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER;
        }

        @Override
        public long getAsLong() {
            ItemResource resource = attachedAccess.getResource();
            UpgradeAware upgradeAware = resource.getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            UpgradeAmount installedEnergy = upgradeAware.getUpgradeCount(UpgradeIds.ENERGY);
            if (!installedEnergy.equals(lastInstalledEnergy)) {
                lastInstalledEnergy = installedEnergy;
                value = MathUtils.clampToLong(attributeEnergy.getBaseMultiplier() * maxEnergyPerTick * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), lastInstalledEnergy.fraction() - 1));
            }
            return value;
        }
    }
}