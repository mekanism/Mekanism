package mekanism.common.tile.component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {
    private static final TicketType<TileComponentChunkLoader<?>> TICKET_TYPE = TicketType.create("mekanism:chunk_loader", Comparator.comparing(tccl->tccl.tile.getPos()));

    /**
     * TileEntity implementing this component.
     */
    public T tile;

    public Set<ChunkPos> chunkSet = new HashSet<>();

    private DimensionType prevDimension;
    private BlockPos prevPos;
    private boolean hasRegistered;

    public TileComponentChunkLoader(T tile) {
        this.tile = tile;
        tile.addComponent(this);
    }


    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().getInstalledTypes().contains(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets()
    {
        ServerChunkProvider chunkProvider = (ServerChunkProvider)tile.getWorld().getChunkProvider();
        chunkProvider.func_217222_b(TICKET_TYPE, new ChunkPos(prevPos), 32, this);
        this.hasRegistered = false;
    }

    @Override
    public void tick() {
        if (tile.getWorld() == null || !(tile.getWorld().getChunkProvider() instanceof ServerChunkProvider)) {
            return;
        }
        if (!tile.isRemote()) {
            if (hasRegistered && (prevDimension == null || prevPos == null || prevDimension != tile.getWorld().dimension.getType() || prevPos != tile.getPos())) {
                releaseChunkTickets();
            }

            if (hasRegistered && !canOperate()) {
                releaseChunkTickets();
            }

            if (canOperate() && !hasRegistered) {
                prevPos = tile.getPos();
                prevDimension = tile.getWorld().dimension.getType();

                ServerChunkProvider chunkProvider = (ServerChunkProvider)tile.getWorld().getChunkProvider();
                chunkProvider.func_217228_a(TICKET_TYPE, new ChunkPos(prevPos), 32, this);
                hasRegistered = true;
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        //prevCoord = Coord4D.read(nbtTags.getCompound("prevCoord"));
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
        /*if (prevCoord != null) {
            nbtTags.put("prevCoord", prevCoord.write(new CompoundNBT()));
        }*/

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
        if (!tile.isRemote()) {
            releaseChunkTickets();
        }
    }
}