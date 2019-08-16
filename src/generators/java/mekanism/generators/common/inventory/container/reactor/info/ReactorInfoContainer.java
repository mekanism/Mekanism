package mekanism.generators.common.inventory.container.reactor.info;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public abstract class ReactorInfoContainer extends MekanismTileContainer<TileEntityReactorController> implements IEmptyContainer {

    protected ReactorInfoContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TileEntityReactorController tileEntityReactorController) {
        super(type, id, inv, tileEntityReactorController);
    }
}