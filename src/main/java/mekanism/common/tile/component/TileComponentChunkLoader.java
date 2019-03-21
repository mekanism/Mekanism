package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.IUpgradeTile;
import mekanism.api.TileNetworkList;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentChunkLoader implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public TileEntityContainerBlock tileEntity;

    public Ticket chunkTicket;

    public Set<ChunkPos> chunkSet = new HashSet<>();

    public Coord4D prevCoord;

    public TileComponentChunkLoader(TileEntityContainerBlock tile) {
        tileEntity = tile;

        tile.components.add(this);
    }

    public void setTicket(Ticket t) {
        if (chunkTicket != t && chunkTicket != null && chunkTicket.world == tileEntity.getWorld()) {
            for (ChunkPos chunk : chunkTicket.getChunkList()) {
                if (ForgeChunkManager.getPersistentChunksFor(tileEntity.getWorld()).keys().contains(chunk)) {
                    ForgeChunkManager.unforceChunk(chunkTicket, chunk);
                }
            }

            ForgeChunkManager.releaseTicket(chunkTicket);
        }

        chunkTicket = t;
    }

    public void release() {
        setTicket(null);
    }

    public void sortChunks() {
        if (chunkTicket != null) {
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
        }
    }

    public void refreshChunkSet() {
        IChunkLoader loader = (IChunkLoader) tileEntity;

        if (!chunkSet.equals(loader.getChunkSet())) {
            chunkSet = loader.getChunkSet();
            sortChunks();
        }
    }

    public void forceChunks(Ticket ticket) {
        setTicket(ticket);

        for (ChunkPos chunk : chunkSet) {
            ForgeChunkManager.forceChunk(chunkTicket, chunk);
        }
    }

    public boolean canOperate() {
        return general.allowChunkloading && ((IUpgradeTile) tileEntity).getComponent().getInstalledTypes()
              .contains(Upgrade.ANCHOR);
    }

    @Override
    public void tick() {
        if (!tileEntity.getWorld().isRemote) {
            if (prevCoord == null || !prevCoord.equals(Coord4D.get(tileEntity))) {
                release();
                prevCoord = Coord4D.get(tileEntity);
            }

            if (chunkTicket != null && (!canOperate() || chunkTicket.world != tileEntity.getWorld())) {
                release();
            }

            refreshChunkSet();

            if (canOperate() && chunkTicket == null) {
                Ticket ticket;
                if (tileEntity instanceof ISecurityTile) {
                    ticket = ForgeChunkManager.requestPlayerTicket(Mekanism.instance,
                          MekanismUtils.getLastKnownUsername(((ISecurityTile) tileEntity).getSecurity().getOwnerUUID()),
                          tileEntity.getWorld(), Type.NORMAL);
                } else {
                    ticket = ForgeChunkManager.requestTicket(Mekanism.instance, tileEntity.getWorld(), Type.NORMAL);
                }

                if (ticket != null) {
                    ticket.getModData().setInteger("x", tileEntity.getPos().getX());
                    ticket.getModData().setInteger("y", tileEntity.getPos().getY());
                    ticket.getModData().setInteger("z", tileEntity.getPos().getZ());

                    forceChunks(ticket);
                }
            }
        }
    }

    @Override
    public void read(NBTTagCompound nbtTags) {
        prevCoord = Coord4D.read(nbtTags.getCompoundTag("prevCoord"));

        chunkSet.clear();
        NBTTagList list = nbtTags.getTagList("chunkSet", NBT.TAG_COMPOUND);

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            chunkSet.add(new ChunkPos(compound.getInteger("chunkX"), compound.getInteger("chunkZ")));
        }
    }

    @Override
    public void read(ByteBuf dataStream) {
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        if (prevCoord != null) {
            nbtTags.setTag("prevCoord", prevCoord.write(new NBTTagCompound()));
        }

        NBTTagList list = new NBTTagList();

        for (ChunkPos pos : chunkSet) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("chunkX", pos.x);
            compound.setInteger("chunkZ", pos.z);
            list.appendTag(compound);
        }

        nbtTags.setTag("chunkSet", list);
    }

    @Override
    public void write(TileNetworkList data) {
    }

    @Override
    public void invalidate() {
        if (!tileEntity.getWorld().isRemote) {
            release();
        }
    }
}
