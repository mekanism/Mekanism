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
import mekanism.client.HolidayManager;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;

public class Machine<TILE extends TileEntityMekanism> {

    private TileEntityTypeRegistryObject<TILE> tileEntityRegistrar;
    private ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar;

    private Supplier<Double> energyUsage;
    private Supplier<Double> energyStorage;

    private SoundEventRegistryObject<SoundEvent> soundRegistrar;

    private MekanismLang description;

    private Set<Upgrade> supportedUpgrades;

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.getTileEntityType();
    }

    public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> getContainerType() {
        return containerRegistrar;
    }

    @Nonnull
    public Set<Upgrade> getSupportedUpgrades() {
        return supportedUpgrades;
    }

    @Nonnull
    public SoundEvent getSoundEvent() {
        return HolidayManager.filterSound(soundRegistrar).getSoundEvent();
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
        
        public FactoryType getFactoryType() {
            return factoryType;
        }
        
        @Nonnull
        public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
            return tierUpgradeMap.get(tier).getBlock().getDefaultState();
        }
    }

    public static class MachineBuilder<TILE extends TileEntityMekanism> {

        private Machine<TILE> holder;
        
        private MachineBuilder(Machine<TILE> holder, TileEntityTypeRegistryObject<TILE> tileEntityRegistrar, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            this.holder = holder;
            holder.tileEntityRegistrar = tileEntityRegistrar;
            holder.containerRegistrar = containerRegistrar;
            holder.description = description;
            holder.soundRegistrar = soundRegistrar;
            holder.supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<TILE> createMachine(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            MachineBuilder<TILE> builder = new MachineBuilder<>(new Machine<>(), tileEntityRegistrar, containerRegistrar, description, soundRegistrar);
            return builder;
        }
        
        public static <TILE extends TileEntityMekanism> MachineBuilder<TILE> createFactoryMachine(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, MekanismLang description, SoundEventRegistryObject<SoundEvent> soundRegistrar, FactoryType factoryType) {
            MachineBuilder<TILE> builder = new MachineBuilder<>(new FactoryMachine<>(), tileEntityRegistrar, containerRegistrar, description, soundRegistrar);
            ((FactoryMachine<TILE>) builder.holder).factoryType = factoryType;
            return builder;
        }

        public MachineBuilder<TILE> withConfig(CachedDoubleValue energyUsage, CachedDoubleValue energyStorage) {
            holder.energyUsage = energyUsage::get;
            holder.energyStorage = energyStorage::get;
            return this;
        }
        
        public MachineBuilder<TILE> withConfig(Supplier<Double> energyUsage, Supplier<Double> energyStorage) {
            holder.energyUsage = energyUsage;
            holder.energyStorage = energyStorage;
            return this;
        }

        public MachineBuilder<TILE> withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.supportedUpgrades = upgrades;
            return this;
        }

        @SafeVarargs
        public final MachineBuilder<TILE> withFactoryHierarchy(BlockRegistryObject<BlockFactory, ItemBlockFactory>... factories) {
            if (!(holder instanceof FactoryMachine)) {
                Mekanism.logger.error("Tried to set a factory hierarchy on a non-factory machine");
                return null;
            }
            
            for (int i = 0; i < factories.length; i++) {
                ((FactoryMachine<TILE>) holder).tierUpgradeMap.put(BaseTier.values()[i], factories[i]);
            }
            return this;
        }

        public Machine<TILE> build() {
            return holder;
        }
    }
}
