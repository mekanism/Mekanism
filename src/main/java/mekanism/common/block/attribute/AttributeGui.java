package mekanism.common.block.attribute;

import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;

public class AttributeGui<TILE extends TileEntityMekanism> implements Attribute {

    private Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar;
    private Function<TILE, INamedContainerProvider> containerSupplier = (tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()),
        (i, inv, player) -> new MekanismTileContainer<>(getContainerType(), i, inv, tile));

    public AttributeGui(Supplier<ContainerTypeRegistryObject<MekanismTileContainer<TILE>>> containerRegistrar) {
        this.containerRegistrar = containerRegistrar;
    }

    public void setCustomContainer(Function<TILE, INamedContainerProvider> containerSupplier) {
        this.containerSupplier = containerSupplier;
    }

    public ContainerTypeRegistryObject<? extends MekanismTileContainer<? super TILE>> getContainerType() {
        return containerRegistrar.get();
    }

    public INamedContainerProvider getProvider(TILE tile) {
        return containerSupplier.apply(tile);
    }
}
