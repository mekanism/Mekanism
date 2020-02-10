package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FactoryContainer extends MekanismTileContainer<TileEntityFactory<?>> {

    public FactoryContainer(int id, PlayerInventory inv, TileEntityFactory<?> tile) {
        super(MekanismContainerTypes.FACTORY, id, inv, tile);
        //TODO: Make it so that syncable data objects we return when on the server are only able to get the data and not set it
        // Also move this various stuff to the tile itself
        trackArray(tile.progress);
        //data.add(recipeTicks);
        track(SyncableBoolean.create(() -> tile.sorting, value -> tile.sorting = value));
        //data.add(lastUsage);
        //data.add(infusionTank.getStack());
        //data.add(gasTank.getStack());
    }

    public FactoryContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFactory.class));
    }

    @Override
    protected int getInventoryYOffset() {
        if (tile.hasSecondaryResourceBar()) {
            return 95;
        }
        if (tile instanceof TileEntitySawingFactory) {
            return 105;
        }
        return 85;
    }

    @Override
    protected int getInventoryXOffset() {
        return tile.tier == FactoryTier.ULTIMATE ? 26 : 8;
    }
}