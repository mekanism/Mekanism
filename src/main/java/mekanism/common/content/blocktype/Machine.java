package mekanism.common.content.blocktype;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;

public class Machine<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    protected DoubleSupplier energyUsage;
    protected DoubleSupplier energyStorage;

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
        return energyUsage.getAsDouble();
    }

    public boolean hasUsage() {
        return energyUsage != null;
    }

    public double getConfigStorage() {
        return energyStorage.getAsDouble();
    }

    public boolean hasConfigStorage() {
        return energyStorage != null;
    }

    public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {

        protected FactoryType factoryType;

        public FactoryMachine(TileEntityTypeRegistryObject<TILE> tileEntitySupplier, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, FactoryType factoryType) {
            super(tileEntitySupplier, containerRegistrar, description);
            this.factoryType = factoryType;
        }

        public FactoryType getFactoryType() {
            return factoryType;
        }

        @Nonnull
        public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
            return MekanismBlocks.getFactory(FactoryTier.values()[tier.ordinal()], factoryType).getBlock().getDefaultState();
        }
    }

    public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends MachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {

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
            builder.holder.factoryType = factoryType;
            return builder;
        }

        public T withConfig(DoubleSupplier energyUsage, DoubleSupplier energyStorage) {
            holder.energyUsage = energyUsage;
            holder.energyStorage = energyStorage;
            return getThis();
        }

        public T withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.supportedUpgrades = upgrades;
            return getThis();
        }
    }
}
