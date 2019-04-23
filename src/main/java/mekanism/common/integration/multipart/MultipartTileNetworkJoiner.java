package mekanism.common.integration.multipart;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartBlockAccess;
import mekanism.common.base.ITileNetwork;
import mekanism.api.TileNetworkList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * Used to route {@link ITileNetwork} packets sent to multipart containers with more than one possible recipient.<br>
 * <br>
 * When MCMP is enabled single byte EnumFacing ordinal headers are added to packets sent by glow panels and transmitters
 * that are then used by this class to route packets to the part attached to the appropriate side.<br>
 * <br>
 * In this case, since transmitters do not attach to a side and therefore have no matching EnumFacing the special value
 * 6 is used to represent the center slot.
 */
public class MultipartTileNetworkJoiner implements ITileNetwork {

    private final HashMap<Byte, ITileNetwork> tileSideMap;

    /**
     * Called by MCMP's multipart container when more than one part implements {@link ITileNetwork}.<br>
     * <br>
     * Builds an internal map of part slots to {@link ITileNetwork} implementations in order to route packets.
     *
     * @param tileList A list of the tile entities that implement {@link ITileNetwork} in the container.
     */
    public MultipartTileNetworkJoiner(List<ITileNetwork> tileList) {
        tileSideMap = new HashMap<>();
        IMultipartContainer container = null;

        TileEntity first = (TileEntity) tileList.get(0);
        IBlockAccess world = first.getWorld();
        if (world instanceof IMultipartBlockAccess) {
            container = ((IMultipartBlockAccess) world).getPartInfo().getContainer();
        } else {
            TileEntity worldTile = first.getWorld().getTileEntity(first.getPos());
            if (worldTile instanceof IMultipartContainer) {
                container = (IMultipartContainer) worldTile;
            }
        }

        if (container != null) {
            for (IPartSlot slot : container.getParts().keySet()) {
                Optional<IMultipartTile> partTile = container.getPartTile(slot);
                if (partTile.isPresent()) {
                    int tileIndex = tileList.indexOf(partTile.get().getTileEntity());
                    if (tileIndex >= 0) {
                        byte slotValue = slot instanceof EnumFaceSlot ? (byte) ((EnumFaceSlot) slot).ordinal() : 6;
                        tileSideMap.put(slotValue, tileList.get(tileIndex));
                    }
                }
            }
        }
    }

    /**
     * Determines whether or not an {@link ITileNetwork} joiner is needed and, if so, adds a single byte header used to
     * route packets inside multipart containers.
     *
     * @param entity The entity for which <code>getNetworkedData</code> is being called
     * @param data The network data list
     * @param facing The side this part is attached to or <code>null</code> for the center slot
     */
    public static void addMultipartHeader(TileEntity entity, TileNetworkList data, EnumFacing facing) {
        int tileNetworkParts = 0;
        IMultipartContainer container = MultipartMekanism.getContainer(entity.getWorld(), entity.getPos());
        if (container != null) {
            for (IPartSlot slot : container.getParts().keySet()) {
                TileEntity part = container.getPartTile(slot)
                      .map(IMultipartTile::getTileEntity)
                      .orElse(null);

                if (part instanceof ITileNetwork) {
                    tileNetworkParts++;
                    if (tileNetworkParts > 1) {
                        break;
                    }
                }
            }
        }

        if (tileNetworkParts > 1) {
            data.add(0, (byte) (facing == null ? 6 : facing.ordinal()));
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        while (dataStream.readableBytes() > 0) {
            byte side = dataStream.readByte();

            ITileNetwork networkTile = tileSideMap.get(side);
            if (networkTile == null) {
                break;
            }

            networkTile.handlePacketData(dataStream);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        TileNetworkList childData = new TileNetworkList();
        for (byte slotValue : tileSideMap.keySet()) {
            tileSideMap.get(slotValue).getNetworkedData(childData);
            data.addAll(childData);
            childData.clear();
        }

        return data;
    }
}
