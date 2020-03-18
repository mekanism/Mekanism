package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;

public class Factory<TILE extends TileEntityFactory<?>> extends FactoryMachine<TILE> {

    private FactoryMachine<?> origMachine;

    public Factory(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, Supplier<ContainerTypeRegistryObject<? extends MekanismTileContainer<?>>> containerRegistrar, FactoryMachine<?> origMachine, FactoryTier tier) {
        super(tileEntityRegistrar, MekanismLang.DESCRIPTION_FACTORY, origMachine.get(AttributeFactoryType.class).getFactoryType());
        this.origMachine = origMachine;
        setMachineData();
        add(new AttributeGui(containerRegistrar), new AttributeTier<>(tier));

        if (tier.ordinal() < FactoryTier.values().length - 1) {
            add(new AttributeUpgradeable(() -> MekanismBlocks.getFactory(FactoryTier.values()[tier.ordinal()+1], origMachine.get(AttributeFactoryType.class).getFactoryType())));
        }
    }

    private void setMachineData() {
        setFrom(origMachine, AttributeSound.class, AttributeFactoryType.class, AttributeUpgradeSupport.class);
        AttributeEnergy origEnergy = origMachine.get(AttributeEnergy.class);
        AttributeTier<FactoryTier> tier = origMachine.get(AttributeTier.class);
        add(new AttributeEnergy(() -> origEnergy.getUsage(), () -> tier.getTier().processes * Math.max(0.5D * origEnergy.getConfigStorage(), origEnergy.getUsage())));
    }

    public static class FactoryBuilder<FACTORY extends Factory<TILE>, TILE extends TileEntityFactory<?>, T extends MachineBuilder<FACTORY, TILE, T>> extends BlockTileBuilder<FACTORY, TILE, T> {

        protected FactoryBuilder(FACTORY holder) {
            super(holder);
        }

        @SuppressWarnings("unchecked")
        public static <TILE extends TileEntityFactory<?>> FactoryBuilder<Factory<TILE>, TILE, ?> createFactory(Supplier<?> tileEntityRegistrar,
              Supplier<?> containerRegistrar, FactoryMachine<?> origMachine, FactoryTier tier) {
            // this is dirty but unfortunately necessary for things to play right
            return new FactoryBuilder<>(new Factory<TILE>((Supplier<TileEntityTypeRegistryObject<TILE>>)tileEntityRegistrar, (Supplier<ContainerTypeRegistryObject<? extends MekanismTileContainer<?>>>)containerRegistrar, origMachine, tier));
        }
    }
}
