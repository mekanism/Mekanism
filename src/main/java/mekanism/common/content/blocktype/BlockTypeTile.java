package mekanism.common.content.blocktype;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeEnergyStorage;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.Nullable;

public class BlockTypeTile<TILE extends TileEntityUpdateable> extends BlockType {

    private final Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

    public BlockTypeTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, IHasTranslationKey description) {
        super(description);
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityTypeRegistryObject<TILE> getTileType() {
        return tileEntityRegistrar.get();
    }

    public static class BlockTileBuilder<BLOCK extends BlockTypeTile<TILE>, TILE extends TileEntityUpdateable, T extends BlockTileBuilder<BLOCK, TILE, T>>
          extends BlockTypeBuilder<BLOCK, T> {

        protected BlockTileBuilder(BLOCK holder) {
            super(holder);
        }

        public static <TILE extends TileEntityUpdateable> BlockTileBuilder<BlockTypeTile<TILE>, TILE, ?> createBlock(
              Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, IHasTranslationKey description) {
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

        public T withEnergyUsage(IntSupplier energyUsage) {
            return withEnergyUsage(energyUsage, 1);
        }

        public T withEnergyUsage(IntSupplier energyUsage, int defaultTicksRequired) {
            return withEnergyUsage(energyUsage,  () -> defaultTicksRequired);
        }

        public T withEnergyUsage(IntSupplier energyUsage, IntSupplier defaultTicksRequired) {
            return with(new AttributeEnergy(energyUsage, defaultTicksRequired));
        }

        public T withEnergyStorage(LongSupplier energyStorage) {
            return with(new AttributeEnergyStorage(energyStorage));
        }

        @SafeVarargs
        public final T with(TileAttribute<TILE>... attrs) {
            holder.add(attrs);
            return self();
        }
    }
}
