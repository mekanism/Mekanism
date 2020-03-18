package mekanism.common.block.attribute;

import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;

public class AttributeGui implements Attribute {

    private Supplier<ContainerTypeRegistryObject<? extends MekanismTileContainer<?>>> containerRegistrar;
    private Function<TileEntityMekanism, INamedContainerProvider> containerSupplier = (tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()),
        (i, inv, player) -> new MekanismTileContainer<>(getContainerType(), i, inv, tile));

    public AttributeGui(Supplier<ContainerTypeRegistryObject<? extends MekanismTileContainer<?>>> containerRegistrar) {
        this.containerRegistrar = containerRegistrar;
    }

    public void setCustomContainer(Function<TileEntityMekanism, INamedContainerProvider> containerSupplier) {
        this.containerSupplier = containerSupplier;
    }

    public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> getContainerType() {
        return containerRegistrar.get();
    }

    public INamedContainerProvider getProvider(TileEntityMekanism tile) {
        return containerSupplier.apply(tile);
    }
}
