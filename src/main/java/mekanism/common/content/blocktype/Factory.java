package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;

public class Factory<TILE extends TileEntityFactory<?>> extends FactoryMachine<TILE> {

    private FactoryMachine<?> origMachine;
    private FactoryTier tier;

    public Factory(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar, FactoryMachine<?> origMachine, FactoryTier tier) {
        super(tileEntityRegistrar, MekanismLang.DESCRIPTION_FACTORY, origMachine.get(AttributeFactoryType.class).getFactoryType());
        this.origMachine = origMachine;
        this.tier = tier;
        setMachineData();
        add(new AttributeGui<>(containerRegistrar));
    }

    private void setMachineData() {
        setFrom(origMachine, AttributeSound.class, AttributeFactoryType.class, AttributeUpgradeSupport.class);
        AttributeEnergy origEnergy = origMachine.get(AttributeEnergy.class);
        add(new AttributeEnergy(() -> origEnergy.getUsage(), () -> tier.processes * Math.max(0.5D * origEnergy.getConfigStorage(), origEnergy.getUsage())));
    }

    public FactoryTier getTier() {
        return tier;
    }

    public static class FactoryBuilder<FACTORY extends Factory<TILE>, TILE extends TileEntityFactory<?>, T extends MachineBuilder<FACTORY, TILE, T>> extends BlockTileBuilder<FACTORY, TILE, T> {

        protected FactoryBuilder(FACTORY holder) {
            super(holder);
        }

        @SuppressWarnings("unchecked")
        public static <TILE extends TileEntityFactory<?>> FactoryBuilder<Factory<TILE>, TILE, ?> createFactory(Supplier<?> tileEntityRegistrar,
              Supplier<?> containerRegistrar, FactoryMachine<?> origMachine, FactoryTier tier) {
            // this is dirty but unfortunately necessary for things to play right
            return new FactoryBuilder<>(new Factory<TILE>((Supplier<TileEntityTypeRegistryObject<TILE>>)tileEntityRegistrar, (Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>>)containerRegistrar, origMachine, tier));
        }
    }
}
