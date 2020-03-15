package mekanism.common.content.blocktype;

import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.HolidayManager;
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

    protected Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;
    protected Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar;
    protected Function<TILE, INamedContainerProvider> customContainerSupplier;

    protected SoundEventRegistryObject<SoundEvent> soundRegistrar;

    protected VoxelShape[] bounds;

    public BlockTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar) {
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.get().getTileEntityType();
    }

    public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> getContainerType() {
        return containerRegistrar.get();
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
