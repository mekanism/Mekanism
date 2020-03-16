package mekanism.common.content.blocktype;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Pos3D;
import mekanism.api.Upgrade;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;

public class Machine<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    protected DoubleSupplier energyUsage;
    protected DoubleSupplier energyStorage;

    protected MekanismLang description;

    protected Set<Upgrade> supportedUpgrades;

    public Machine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
        super(tileEntityRegistrar);
        this.description = description;
        this.supportedUpgrades = EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);

        // add default particle effects
        attributeMap.put(AttributeParticleFX.class, new AttributeParticleFX()
            .add(ParticleTypes.SMOKE, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
            .add(RedstoneParticleData.REDSTONE_DUST, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)));
        attributeMap.put(AttributeStateActive.class, new AttributeStateActive());
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

        public FactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntitySupplier, MekanismLang description, FactoryType factoryType) {
            super(tileEntitySupplier, description);
            attributeMap.put(AttributeFactoryType.class, new AttributeFactoryType(factoryType));
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
