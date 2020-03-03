package mekanism.common.content.machines;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.client.HolidayManager;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.shapes.VoxelShape;

public class BlockTile<TILE extends TileEntityMekanism> {

    protected TileEntityTypeRegistryObject<TILE> tileEntityRegistrar;
    protected ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar;
    protected Function<TILE, INamedContainerProvider> customContainerSupplier;

    protected SoundEventRegistryObject<SoundEvent> soundRegistrar;

    protected VoxelShape[] bounds;

    public BlockTile(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar) {
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.getTileEntityType();
    }

    public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> getContainerType() {
        return containerRegistrar;
    }

    @Nonnull
    public SoundEvent getSoundEvent() {
        return soundRegistrar != null ? HolidayManager.filterSound(soundRegistrar).getSoundEvent() : null;
    }

    public boolean hasSound() {
        return soundRegistrar != null;
    }

    public VoxelShape[] getBounds() {
        return bounds;
    }

    public boolean hasCustomShape() {
        return bounds != null;
    }

    public INamedContainerProvider getCustomContainer(TILE tileEntity) {
        return customContainerSupplier != null ? customContainerSupplier.apply(tileEntity) : null;
    }

    public boolean hasCustomContainer() {
        return customContainerSupplier != null;
    }

    public static class BlockTileBuilder<BLOCK extends BlockTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTileBuilder<BLOCK, TILE, T>> {

        protected BLOCK holder;

        protected BlockTileBuilder(BLOCK holder) {
            this.holder = holder;
        }

        public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            holder.soundRegistrar = soundRegistrar;
            return getThis();
        }

        public T withCustomShape(VoxelShape[] shape) {
            holder.bounds = shape;
            return getThis();
        }

        public T withCustomContainer(Function<TILE, INamedContainerProvider> customContainerSupplier) {
            holder.customContainerSupplier = customContainerSupplier;
            return getThis();
        }

        @SuppressWarnings("unchecked")
        public T getThis() {
            return (T) this;
        }

        public BLOCK build() {
            return holder;
        }
    }
}
