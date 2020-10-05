package mekanism.common.content.blocktype;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;

public class BlockTypeTile<TILE extends TileEntityMekanism> extends BlockType {

    private final Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

    public BlockTypeTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(description);
        this.tileEntityRegistrar = tileEntityRegistrar;
    }

    public TileEntityType<TILE> getTileType() {
        return tileEntityRegistrar.get().getTileEntityType();
    }

    public static class BlockTileBuilder<BLOCK extends BlockTypeTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTileBuilder<BLOCK, TILE, T>> extends BlockTypeBuilder<BLOCK, T> {

        protected BlockTileBuilder(BLOCK holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> BlockTileBuilder<BlockTypeTile<TILE>, TILE, ?> createBlock(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
            return new BlockTileBuilder<>(new BlockTypeTile<>(tileEntityRegistrar, description));
        }

        public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
            return with(new AttributeSound(soundRegistrar));
        }

        public T withGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar) {
            return with(new AttributeGui(containerRegistrar));
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

        public T withNamedContainerProvider(Function<TileEntityMekanism, INamedContainerProvider> customContainerSupplier) {
            if (!holder.has(AttributeGui.class)) {
                Mekanism.logger.error("Attempted to set a custom container on a block type without a GUI attribute.");
            }
            holder.get(AttributeGui.class).setCustomContainer(customContainerSupplier);
            return getThis();
        }

        public T withCustomContainerProvider(Function<TileEntityMekanism, IContainerProvider> providerFunction) {
            return withNamedContainerProvider(tile -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), providerFunction.apply(tile)));
        }

        public T withEmptyContainer(ContainerTypeRegistryObject<?> container) {
            return withCustomContainerProvider(tile -> ((i, inv, player) -> new EmptyTileContainer<>(container, i, inv, tile)));
        }

        public T withSupportedUpgrades(Set<Upgrade> upgrades) {
            holder.add(new AttributeUpgradeSupport(upgrades));
            return getThis();
        }
    }
}
