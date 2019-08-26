package mekanism.common.tile.interfaces;

import mekanism.api.TileNetworkList;
import mekanism.common.HashList;
import mekanism.common.content.filter.IFilter;

public interface ITileFilterHolder<FILTER extends IFilter> {

    //TODO: Decide if we want to keep using HashList
    HashList<FILTER> getFilters();

    TileNetworkList getFilterPacket(TileNetworkList data);

    default TileNetworkList getFilterPacket() {
        return getFilterPacket(new TileNetworkList());
    }
}