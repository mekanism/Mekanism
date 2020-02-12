package mekanism.common.block.interfaces;

import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;

public interface IHasGui<TILE extends TileEntityMekanism> {

    ContainerTypeRegistryObject<? extends MekanismTileContainer<? super TILE>> getContainerType();

    default INamedContainerProvider getProvider(TILE tile) {
        return new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()),
              (i, inv, player) -> new MekanismTileContainer<>(getContainerType(), i, inv, tile));
    }
}