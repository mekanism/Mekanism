package mekanism.common.content.blocktype;

import java.util.EnumSet;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;

public class Machine<TILE extends TileEntityMekanism> extends BlockTypeTile<TILE> {

    public Machine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
        super(tileEntityRegistrar, description);

        // add default particle effects
        add(new AttributeParticleFX()
              .add(ParticleTypes.SMOKE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, 0.52))
              .add(RedstoneParticleData.REDSTONE_DUST, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, 0.52)));
        add(Attributes.ACTIVE, new AttributeStateFacing(), Attributes.INVENTORY, Attributes.SECURITY, Attributes.REDSTONE, Attributes.COMPARATOR);
        add(new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING)));
    }

    public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {

        public FactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntitySupplier, MekanismLang description, FactoryType factoryType) {
            super(tileEntitySupplier, description);
            add(new AttributeFactoryType(factoryType), new AttributeUpgradeable(() -> MekanismBlocks.getFactory(FactoryTier.BASIC, get(AttributeFactoryType.class).getFactoryType())));
        }
    }

    public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends MachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {

        protected MachineBuilder(MACHINE holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<Machine<TILE>, TILE, ?> createMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
            return new MachineBuilder<>(new Machine<>(tileEntityRegistrar, description));
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<FactoryMachine<TILE>, TILE, ?> createFactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              MekanismLang description, FactoryType factoryType) {
            return new MachineBuilder<>(new FactoryMachine<>(tileEntityRegistrar, description, factoryType));
        }
    }
}
