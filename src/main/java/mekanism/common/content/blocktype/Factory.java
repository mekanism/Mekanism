package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.particles.ParticleTypes;

public class Factory<TILE extends TileEntityFactory<?>> extends FactoryMachine<TILE> {

    private final FactoryMachine<?> origMachine;

    public Factory(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar,
          FactoryMachine<?> origMachine, FactoryTier tier) {
        super(tileEntityRegistrar, MekanismLang.DESCRIPTION_FACTORY, origMachine.getFactoryType());
        this.origMachine = origMachine;
        setMachineData(tier);
        add(new AttributeGui(containerRegistrar, null), new AttributeTier<>(tier));

        if (tier.ordinal() < EnumUtils.FACTORY_TIERS.length - 1) {
            add(new AttributeUpgradeable(() -> MekanismBlocks.getFactory(EnumUtils.FACTORY_TIERS[tier.ordinal() + 1], origMachine.getFactoryType())));
        }
    }

    private void setMachineData(FactoryTier tier) {
        setFrom(origMachine, AttributeSound.class, AttributeFactoryType.class, AttributeUpgradeSupport.class);
        AttributeEnergy origEnergy = origMachine.get(AttributeEnergy.class);
        add(new AttributeEnergy(origEnergy::getUsage, () -> MathUtils.clampToLong(Math.max(origEnergy.getConfigStorage() * 0.5, origEnergy.getUsage()) * tier.processes)));
    }

    public static class FactoryBuilder<FACTORY extends Factory<TILE>, TILE extends TileEntityFactory<?>, T extends MachineBuilder<FACTORY, TILE, T>>
          extends BlockTileBuilder<FACTORY, TILE, T> {

        protected FactoryBuilder(FACTORY holder) {
            super(holder);
        }

        @SuppressWarnings("unchecked")
        public static <TILE extends TileEntityFactory<?>> FactoryBuilder<Factory<TILE>, TILE, ?> createFactory(Supplier<?> tileEntityRegistrar, FactoryType type,
              FactoryTier tier) {
            // this is dirty but unfortunately necessary for things to play right
            FactoryBuilder<Factory<TILE>, TILE, ?> builder = new FactoryBuilder<>(new Factory<>((Supplier<TileEntityTypeRegistryObject<TILE>>) tileEntityRegistrar,
                  () -> MekanismContainerTypes.FACTORY, type.getBaseMachine(), tier));
            //Note, we can't just return the builder here as then it gets all confused about object types, so we just
            // assign the value here, and then return the builder itself as it is the same object
            builder.withComputerSupport(tier, type.getRegistryNameComponentCapitalized() + "Factory");
            builder.withCustomShape(BlockShapes.getShape(tier, type));
            builder.with(switch (type) {
                case SMELTING, ENRICHING, CRUSHING, COMBINING, SAWING -> AttributeSideConfig.ELECTRIC_MACHINE;
                case COMPRESSING, INJECTING, PURIFYING, INFUSING -> AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE;
            });
            builder.replace(new AttributeParticleFX().addDense(ParticleTypes.SMOKE, 5, rand -> new Pos3D(
                  rand.nextFloat() * 0.7F - 0.3F,
                  rand.nextFloat() * 0.1F + 0.7F,
                  rand.nextFloat() * 0.7F - 0.3F
            )));
            return builder;
        }
    }
}
