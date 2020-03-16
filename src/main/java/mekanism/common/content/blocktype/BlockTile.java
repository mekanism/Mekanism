package mekanism.common.content.blocktype;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
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
import net.minecraft.util.math.shapes.VoxelShape;

public class BlockTile<TILE extends TileEntityMekanism> {

    protected Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

    protected Map<Class<? extends Attribute>, Attribute> attributeMap = new HashMap<>();

    public BlockTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar) {
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public boolean has(Class<? extends Attribute> type) {
        return attributeMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T get(Class<T> type) {
        return (T)attributeMap.get(type);
    }

    @SafeVarargs
    protected final void setFrom(BlockTile<?> tile, Class<? extends Attribute>... types) {
        for (Class<? extends Attribute> type : types) {
            attributeMap.put(type, tile.get(type));
        }
    }

    public void remove(Class<? extends Attribute> type) {
        attributeMap.remove(type);
    }

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.get().getTileEntityType();
    }

    public static class BlockTileBuilder<BLOCK extends BlockTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTileBuilder<BLOCK, TILE, T>> {

        protected BLOCK holder;

        protected BlockTileBuilder(BLOCK holder) {
            this.holder = holder;
        }

        @SuppressWarnings("unchecked")
        public T getThis() {
            return (T) this;
        }

        public T with(Attribute attr) {
            holder.attributeMap.put(attr.getClass(), attr);
            return getThis();
        }

        public T without(Class<? extends Attribute> attr) {
            holder.remove(attr);
            return getThis();
        }

        public T withCustomShape(VoxelShape[] shape) {
            return with(new AttributeCustomShape(shape));
        }

        public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            return with(new AttributeSound(soundRegistrar));
        }

        public T withGui(Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar) {
            return with(new AttributeGui<>(containerRegistrar));
        }

        @SuppressWarnings("unchecked")
        public T withCustomContainer(Function<TILE, INamedContainerProvider> customContainerSupplier) {
            if (!holder.has(AttributeGui.class)) {
                Mekanism.logger.error("Attempted to set a custom container on a block type without a GUI attribute.");
            }
            holder.get(AttributeGui.class).setCustomContainer(customContainerSupplier);
            return getThis();
        }

        public BLOCK build() {
            return holder;
        }
    }
}
