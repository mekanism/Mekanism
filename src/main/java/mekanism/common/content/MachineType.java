package mekanism.common.content;

import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.api.tier.BaseTier;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.block.prefab.BlockOperationalMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.block.machine.ItemBlockOperationalMachine;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import net.minecraft.util.SoundEvent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MachineType<TILE extends TileEntityOperationalMachine<?>> {
    private String name;
    private Class<TILE> tileClass;
    private Supplier<Double> usageSupplier;
    private Supplier<Double> storageSupplier;
    private FactoryType factoryType;
    private ILangEntry description;

    private Map<BaseTier, BlockRegistryObject<BlockFactory, ItemBlockFactory>> factoryTiers = new HashMap<>();
    private Set<Upgrade> supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);

    private BlockRegistryObject<BlockOperationalMachine<TILE>, ItemBlockOperationalMachine<TILE>> blockRegistry;
    private ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistry;
    private TileEntityTypeRegistryObject<TILE> tileRegistry;
    private SoundEventRegistryObject<SoundEvent> soundRegistry;

    public MachineType(String name, Class<TILE> tileClass, ILangEntry description, Supplier<Double> usageSupplier, Supplier<Double> storageSupplier, FactoryType factoryType) {
        this.name = name;
        this.tileClass = tileClass;
        this.description = description;
        this.usageSupplier = usageSupplier;
        this.storageSupplier = storageSupplier;
        this.factoryType = factoryType;
    }

    //TODO: populate factory map
    public void addFactory(BaseTier tier, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory) {
        factoryTiers.put(tier, factory);
    }

    public MachineType<TILE> supportsGas() {
        supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS);
        return this;
    }

    public BlockRegistryObject<BlockFactory, ItemBlockFactory> getFactory(BaseTier tier) {
        return factoryTiers.get(tier);
    }

    public void register() {
        Supplier<BlockOperationalMachine<TILE>> blockSupplier = () -> new BlockOperationalMachine<TILE>(this);
        blockRegistry = MekanismBlocks.BLOCKS.register(name, blockSupplier, ItemBlockOperationalMachine::new);
        containerRegistry = MekanismContainerTypes.CONTAINER_TYPES.register(blockRegistry, tileClass);
        tileRegistry = MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(blockRegistry, this::getTileEntity);
        soundRegistry = MekanismSounds.SOUND_EVENTS.register("tile.machine." + name);
    }

    public String getName() {
        return name;
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public SoundEvent getSound() {
        return soundRegistry.getSoundEvent();
    }

    public TILE getTileEntity() {
        try {
            return tileClass.newInstance();
        } catch(Exception e) {
            System.err.println("Could not instantiate TileEntity of type " + name + ". Make sure a no-arg construct exists.");
            return null;
        }
    }

    public ILangEntry getDescription() {
        return description;
    }

    public double getUsage() {
        return usageSupplier.get();
    }

    public double getStorage() {
        return storageSupplier.get();
    }

    public BlockRegistryObject<BlockOperationalMachine<TILE>, ItemBlockOperationalMachine<TILE>> getBlockType() {
        return blockRegistry;
    }

    public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> getContainerType() {
        return containerRegistry;
    }

    public TileEntityTypeRegistryObject<TILE> getTileType() {
        return tileRegistry;
    }

    public Set<Upgrade> getSupportedUpgrade() {
        return supportedUpgrades;
    }
}
