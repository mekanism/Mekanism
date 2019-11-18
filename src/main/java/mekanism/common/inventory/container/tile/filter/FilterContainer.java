package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nullable;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;

public abstract class FilterContainer<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>> extends MekanismTileContainer<TILE> {

    protected FILTER filter;
    protected FILTER origFilter;

    protected FilterContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, TILE tile, int index) {
        super(type, id, inv, tile);
        if (index >= 0) {
            //TODO: Should this somehow be checked to verify it is the correct type
            origFilter = (FILTER) tile.getFilters().get(index);
        }
        if (origFilter == null) {
            filter = createNewFilter();
        } else {
            filter = origFilter.clone();
        }
        //TODO: FIXME, the slots are slightly offset
    }

    @Override
    public void addSlots() {
        //TODO: Check if any filters actually have us wanting to add the slots
        //NO-OP
    }

    public boolean isNew() {
        return origFilter == null;
    }

    public FILTER getFilter() {
        return filter;
    }

    public FILTER getOrigFilter() {
        return origFilter;
    }

    public abstract FILTER createNewFilter();
}