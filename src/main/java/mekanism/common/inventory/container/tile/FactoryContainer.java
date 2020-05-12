package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FactoryContainer extends MekanismTileContainer<TileEntityFactory<?>> {

    public FactoryContainer(int id, PlayerInventory inv, TileEntityFactory<?> tile) {
        super(MekanismContainerTypes.FACTORY, id, inv, tile);
    }

    public FactoryContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFactory.class));
    }

    @Override
    protected int getInventoryYOffset() {
        if (tile != null) {
            if (tile.hasSecondaryResourceBar()) {
                return 95;
            } else if (tile instanceof TileEntitySawingFactory) {
                return 105;
            }
        }
        return 85;
    }

    @Override
    protected int getInventoryXOffset() {
        return tile != null && tile.tier == FactoryTier.ULTIMATE ? 26 : 8;
    }
}
