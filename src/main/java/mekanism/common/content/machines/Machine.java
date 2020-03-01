package mekanism.common.content.machines;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;

public class Machine<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    protected Supplier<Double> energyUsage;
    protected Supplier<Double> energyStorage;

    protected MekanismLang description;

    protected Set<Upgrade> supportedUpgrades;
    
    public Machine(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description) {
        super(tileEntityRegistrar);
        this.containerRegistrar = containerRegistrar;
        this.description = description;
        this.supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    @Nonnull
    public Set<Upgrade> getSupportedUpgrades() {
        return supportedUpgrades;
    }

    @Nonnull
    public ILangEntry getDescription() {
        return description;
    }

    public double getUsage() {
        return energyUsage.get();
    }

    public double getConfigStorage() {
        return energyStorage.get();
    }

    public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {
        private FactoryType factoryType;

        private Map<BaseTier, BlockRegistryObject<BlockFactory, ItemBlockFactory>> tierUpgradeMap = new HashMap<>();
        
        public FactoryMachine(TileEntityTypeRegistryObject<TILE> tileEntitySupplier, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, FactoryType factoryType) {
            super(tileEntitySupplier, containerRegistrar, description);
            this.factoryType = factoryType;
        }

        public FactoryType getFactoryType() {
            return factoryType;
        }

        @Nonnull
        public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
            return tierUpgradeMap.get(tier).getBlock().getDefaultState();
        }
    }

    public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends MachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {

        private MACHINE holder;

        protected MachineBuilder(MACHINE holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<Machine<TILE>, TILE, ?> createMachine(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar,
            ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description) {
            return new MachineBuilder<>(new Machine<TILE>(tileEntityRegistrar, containerRegistrar, description));
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<FactoryMachine<TILE>, TILE, ?> createFactoryMachine(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar,
            ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, FactoryType factoryType) {
            MachineBuilder<FactoryMachine<TILE>, TILE, ?> builder = new MachineBuilder<>(new FactoryMachine<>(tileEntityRegistrar, containerRegistrar, description, factoryType));
            ((FactoryMachine<TILE>) builder.holder).factoryType = factoryType;
            return builder;
        }

        public T withConfig(CachedDoubleValue energyUsage, CachedDoubleValue energyStorage) {
            holder.energyUsage = energyUsage::get;
            holder.energyStorage = energyStorage::get;
            return getThis();
        }

        public T withConfig(Supplier<Double> energyUsage, Supplier<Double> energyStorage) {
            holder.energyUsage = energyUsage;
            holder.energyStorage = energyStorage;
            return getThis();
        }

        public T withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.supportedUpgrades = upgrades;
            return getThis();
        }

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public final T withFactoryHierarchy(BlockRegistryObject<BlockFactory, ItemBlockFactory>... factories) {
            if (!(holder instanceof FactoryMachine)) {
                Mekanism.logger.error("Tried to set a factory hierarchy on a non-factory machine");
                return null;
            }

            for (int i = 0; i < factories.length; i++) {
                ((FactoryMachine<TILE>) holder).tierUpgradeMap.put(BaseTier.values()[i], factories[i]);
            }
            return getThis();
        }

        public MACHINE build() {
            return holder;
        }
    }
}
