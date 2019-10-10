package mekanism.common.tile.component;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.Ticket;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentChunkLoader implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public TileEntityMekanism tileEntity;

    public Ticket chunkTicket;

    public Set<ChunkPos> chunkSet = new HashSet<>();

    public Coord4D prevCoord;

    public TileComponentChunkLoader(TileEntityMekanism tile) {
        tileEntity = tile;
        tile.addComponent(this);
    }

    public void setTicket(Ticket t) {
        //TODO: Chunk Loading
        /*if (chunkTicket != t && chunkTicket != null && chunkTicket.world == tileEntity.getWorld()) {
            for (ChunkPos chunk : chunkTicket.getChunkList()) {
                if (ForgeChunkManager.getPersistentChunksFor(tileEntity.getWorld()).keys().contains(chunk)) {
                    ForgeChunkManager.unforceChunk(chunkTicket, chunk);
                }
            }
            ForgeChunkManager.releaseTicket(chunkTicket);
        }*/
        chunkTicket = t;
    }

    public void release() {
        setTicket(null);
    }

    public void sortChunks() {
        //TODO: Chunk Loading
        /*if (chunkTicket != null) {
            for (ChunkPos chunk : chunkTicket.getChunkList()) {
                if (!chunkSet.contains(chunk)) {
                    if (ForgeChunkManager.getPersistentChunksFor(tileEntity.getWorld()).keys().contains(chunk)) {
                        ForgeChunkManager.unforceChunk(chunkTicket, chunk);
                    }
                }
            }
            for (ChunkPos chunk : chunkSet) {
                if (!chunkTicket.getChunkList().contains(chunk)) {
                    ForgeChunkManager.forceChunk(chunkTicket, chunk);
                }
            }
        }*/
    }

    public void refreshChunkSet() {
        IChunkLoader loader = (IChunkLoader) tileEntity;
        if (!chunkSet.equals(loader.getChunkSet())) {
            chunkSet = loader.getChunkSet();
            sortChunks();
        }
    }

    //TODO: Chunk Loading
    /*public void forceChunks(Ticket ticket) {
        setTicket(ticket);
        for (ChunkPos chunk : chunkSet) {
            ForgeChunkManager.forceChunk(chunkTicket, chunk);
        }
    }*/

    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && ((IUpgradeTile) tileEntity).getComponent().getInstalledTypes().contains(Upgrade.ANCHOR);
    }

    @Override
    public void tick() {
        if (!tileEntity.isRemote()) {
            if (prevCoord == null || !prevCoord.equals(Coord4D.get(tileEntity))) {
                release();
                prevCoord = Coord4D.get(tileEntity);
            }

            //TODO: Chunk Loading
            /*if (chunkTicket != null && (!canOperate() || chunkTicket.world != tileEntity.getWorld())) {
                release();
            }*/

            refreshChunkSet();

            if (canOperate() && chunkTicket == null) {
                //TODO: Chunk loading
                /*Ticket ticket;
                if (tileEntity.hasSecurity()) {
                    ticket = ForgeChunkManager.requestPlayerTicket(Mekanism.instance,
                          MekanismUtils.getLastKnownUsername(((ISecurityTile) tileEntity).getSecurity().getOwnerUUID()), tileEntity.getWorld(), Type.NORMAL);
                } else {
                    ticket = ForgeChunkManager.requestTicket(Mekanism.instance, tileEntity.getWorld(), Type.NORMAL);
                }

                if (ticket != null) {
                    ticket.getModData().putInt("x", tileEntity.getPos().getX());
                    ticket.getModData().putInt("y", tileEntity.getPos().getY());
                    ticket.getModData().putInt("z", tileEntity.getPos().getZ());
                    forceChunks(ticket);
                }*/
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        prevCoord = Coord4D.read(nbtTags.getCompound("prevCoord"));
        chunkSet.clear();
        ListNBT list = nbtTags.getList("chunkSet", NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT compound = list.getCompound(i);
            chunkSet.add(new ChunkPos(compound.getInt("chunkX"), compound.getInt("chunkZ")));
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        if (prevCoord != null) {
            nbtTags.put("prevCoord", prevCoord.write(new CompoundNBT()));
        }

        ListNBT list = new ListNBT();
        for (ChunkPos pos : chunkSet) {
            CompoundNBT compound = new CompoundNBT();
            compound.putInt("chunkX", pos.x);
            compound.putInt("chunkZ", pos.z);
            list.add(compound);
        }
        nbtTags.put("chunkSet", list);
    }

    @Override
    public void write(TileNetworkList data) {
    }

    @Override
    public void invalidate() {
        if (!tileEntity.isRemote()) {
            release();
        }
    }
}