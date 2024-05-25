package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class Machine<TILE extends TileEntityMekanism> extends BlockTypeTile<TILE> {

    public Machine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(tileEntityRegistrar, description);
        // add default particle effects
        add(new AttributeParticleFX()
              .add(ParticleTypes.SMOKE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, 0.52))
              .add(DustParticleOptions.REDSTONE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, 0.52)));
        add(Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.INVENTORY, Attributes.SECURITY, Attributes.REDSTONE, Attributes.COMPARATOR,
              AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES);
    }

    public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {

        public FactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntitySupplier, ILangEntry description, FactoryType factoryType) {
            super(tileEntitySupplier, description);
            add(new AttributeFactoryType(factoryType), new AttributeUpgradeable(() -> MekanismBlocks.getFactory(FactoryTier.BASIC, getFactoryType())));
        }

        public FactoryType getFactoryType() {
            return get(AttributeFactoryType.class).getFactoryType();
        }
    }

    public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends MachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {

        protected MachineBuilder(MACHINE holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<Machine<TILE>, TILE, ?> createMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description) {
            return new MachineBuilder<>(new Machine<>(tileEntityRegistrar, description));
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<FactoryMachine<TILE>, TILE, ?> createFactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description, FactoryType factoryType) {
            return new MachineBuilder<>(new FactoryMachine<>(tileEntityRegistrar, description, factoryType));
        }
    }
}
