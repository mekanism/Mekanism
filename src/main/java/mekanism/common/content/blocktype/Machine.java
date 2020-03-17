package mekanism.common.content.blocktype;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Pos3D;
import mekanism.api.Upgrade;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;

public class Machine<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    public Machine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
        super(tileEntityRegistrar, description);

        // add default particle effects
        add(new AttributeParticleFX()
            .add(ParticleTypes.SMOKE, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
            .add(RedstoneParticleData.REDSTONE_DUST, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)));
        add(new AttributeStateActive(), new AttributeInventory(), new AttributeSecurity(), new AttributeRedstone(), new AttributeComparator());
        add(new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING)));
    }

    public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {

        public FactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntitySupplier, MekanismLang description, FactoryType factoryType) {
            super(tileEntitySupplier, description);
            add(new AttributeFactoryType(factoryType));
        }

        @Nonnull
        public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
            return MekanismBlocks.getFactory(FactoryTier.values()[tier.ordinal()], get(AttributeFactoryType.class).getFactoryType()).getBlock().getDefaultState();
        }
    }

    public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends MachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {

        protected MachineBuilder(MACHINE holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<Machine<TILE>, TILE, ?> createMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
            return new MachineBuilder<>(new Machine<TILE>(tileEntityRegistrar, description));
        }

        public static <TILE extends TileEntityMekanism> MachineBuilder<FactoryMachine<TILE>, TILE, ?> createFactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              MekanismLang description, FactoryType factoryType) {
            MachineBuilder<FactoryMachine<TILE>, TILE, ?> builder = new MachineBuilder<>(new FactoryMachine<>(tileEntityRegistrar, description, factoryType));
            return builder;
        }

        public T withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.add(new AttributeUpgradeSupport(upgrades));
            return getThis();
        }
    }
}
