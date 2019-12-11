package mekanism.generators.common.inventory.container.reactor.info;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;

public abstract class ReactorInfoContainer extends MekanismTileContainer<TileEntityReactorController> implements IEmptyContainer {

    protected ReactorInfoContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, TileEntityReactorController tile) {
        super(type, id, inv, tile);
    }
}