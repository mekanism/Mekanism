package mekanism.common.content;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.ITileEntityTypeProvider;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.prefab.BlockOperationalMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.machine.ItemBlockOperationalMachine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;

public class MachineType<TILE extends TileEntityOperationalMachine<?>> implements IBlockProvider, ITileEntityTypeProvider<TILE> {

    private DoubleSupplier usageSupplier;
    private DoubleSupplier storageSupplier;
    private ILangEntry description;

    private Set<Upgrade> supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);

    private BlockRegistryObject<BlockOperationalMachine<TILE>, ItemBlockOperationalMachine<TILE>> blockRegistry;
    private ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistry;
    private TileEntityTypeRegistryObject<TILE> tileRegistry;
    private SoundEventRegistryObject<SoundEvent> soundRegistry;

    public MachineType(String name, Class<TILE> tileClass, ILangEntry description, DoubleSupplier usageSupplier, DoubleSupplier storageSupplier,
          Supplier<? extends TILE> tileFactory, FactoryType factoryType) {
        this.description = description;
        this.usageSupplier = usageSupplier;
        this.storageSupplier = storageSupplier;
        this.blockRegistry = MekanismBlocks.BLOCKS.register(name, () -> new BlockOperationalMachine<>(this, factoryType), ItemBlockOperationalMachine::new);
        this.containerRegistry = MekanismContainerTypes.CONTAINER_TYPES.register(this.blockRegistry, tileClass);
        this.tileRegistry = MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(this.blockRegistry, tileFactory);
        this.soundRegistry = MekanismSounds.SOUND_EVENTS.register("tile.machine." + name);
    }

    public MachineType<TILE> supportsGas() {
        supportedUpgrades.add(Upgrade.GAS);
        return this;
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return blockRegistry.getBlock();
    }

    public SoundEvent getSound() {
        return soundRegistry.getSoundEvent();
    }

    public ILangEntry getDescription() {
        return description;
    }

    public double getUsage() {
        return usageSupplier.getAsDouble();
    }

    public double getStorage() {
        return storageSupplier.getAsDouble();
    }

    public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> getContainerType() {
        return containerRegistry;
    }

    @Nonnull
    @Override
    public TileEntityType<TILE> getTileEntityType() {
        return tileRegistry.getTileEntityType();
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedUpgrades;
    }

    @Nonnull
    @Override
    public Item getItem() {
        return blockRegistry.getItem();
    }
}
