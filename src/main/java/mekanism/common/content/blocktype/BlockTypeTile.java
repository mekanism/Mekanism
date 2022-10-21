package mekanism.common.content.blocktype;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class BlockTypeTile<TILE extends TileEntityMekanism> extends BlockType {

    private final Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

    public BlockTypeTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(description);
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityTypeRegistryObject<TILE> getTileType() {
        return tileEntityRegistrar.get();
    }

    public static class BlockTileBuilder<BLOCK extends BlockTypeTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTileBuilder<BLOCK, TILE, T>>
          extends BlockTypeBuilder<BLOCK, T> {

        protected BlockTileBuilder(BLOCK holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> BlockTileBuilder<BlockTypeTile<TILE>, TILE, ?> createBlock(
              Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
            return new BlockTileBuilder<>(new BlockTypeTile<>(tileEntityRegistrar, description));
        }

        public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            return with(new AttributeSound(soundRegistrar));
        }

        public T withGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar) {
            return withGui(containerRegistrar, null);
        }

        public T withGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar, @Nullable ILangEntry customName) {
            return with(new AttributeGui(containerRegistrar, customName));
        }

        public T withEnergyConfig(FloatingLongSupplier energyUsage, FloatingLongSupplier energyStorage) {
            return with(new AttributeEnergy(energyUsage, energyStorage));
        }

        public T withEnergyConfig(FloatingLongSupplier energyStorage) {
            return with(new AttributeEnergy(null, energyStorage));
        }

        @SafeVarargs
        public final T with(TileAttribute<TILE>... attrs) {
            holder.add(attrs);
            return getThis();
        }

        public T withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.add(new AttributeUpgradeSupport(upgrades));
            return getThis();
        }
    }
}
