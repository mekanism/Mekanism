package mekanism.common.content.blocktype;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;

public class BlockTypeTile<TILE extends TileEntityMekanism> extends BlockType {

    private Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

    public BlockTypeTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(description);
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.get().getTileEntityType();
    }

    public static class BlockTileBuilder<BLOCK extends BlockTypeTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTileBuilder<BLOCK, TILE, T>> extends BlockTypeBuilder<BLOCK, T> {

        protected BLOCK holder;

        protected BlockTileBuilder(BLOCK holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> BlockTileBuilder<BlockTypeTile<TILE>, TILE, ?> createBlock(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
            return new BlockTileBuilder<>(new BlockTypeTile<TILE>(tileEntityRegistrar, description));
        }

        public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            return with(new AttributeSound(soundRegistrar));
        }

        public T withGui(Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar) {
            return with(new AttributeGui<>(containerRegistrar));
        }

        public T withEnergyConfig(DoubleSupplier energyUsage, DoubleSupplier energyStorage) {
            return with(new AttributeEnergy(energyUsage, energyStorage));
        }

        public T withEnergyConfig(DoubleSupplier energyStorage) {
            return with(new AttributeEnergy(null, energyStorage));
        }

        @SuppressWarnings("unchecked")
        public T withCustomContainer(Function<TILE, INamedContainerProvider> customContainerSupplier) {
            if (!holder.has(AttributeGui.class)) {
                Mekanism.logger.error("Attempted to set a custom container on a block type without a GUI attribute.");
            }
            holder.get(AttributeGui.class).setCustomContainer(customContainerSupplier);
            return getThis();
        }
    }
}
