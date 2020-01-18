package mekanism.common.tile.component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.chunkloading.ChunkManager;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {

    private static final TicketType<TileComponentChunkLoader<?>> TICKET_TYPE = TicketType.create("mekanism:chunk_loader", Comparator.comparing(tccl -> tccl.tile.getPos()));
    /**
     * Not 100% sure what this is, but 2 means the ticket has the same value as a forceChunk()
     */
    public static final int TICKET_DISTANCE = 2;

    /**
     * TileEntity implementing this component.
     */
    private T tile;

    private Set<ChunkPos> chunkSet = new HashSet<>();

    @Nullable
    private World prevWorld;
    @Nullable
    private BlockPos prevPos;

    private boolean hasRegistered;

    public TileComponentChunkLoader(T tile) {
        this.tile = tile;
        tile.addComponent(this);
    }


    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().getInstalledTypes().contains(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets(@Nonnull World world) {
        ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
        Iterator<ChunkPos> chunkIt = chunkSet.iterator();
        ChunkManager manager = ChunkManager.getInstance((ServerWorld) world);
        while (chunkIt.hasNext()) {
            ChunkPos chunkPos = chunkIt.next();
            if (prevPos != null) {
                manager.deregisterChunk(chunkPos, prevPos);
            }
            chunkProvider.func_217222_b(TICKET_TYPE, chunkPos, TICKET_DISTANCE, this);
            chunkIt.remove();
        }
        this.hasRegistered = false;
        this.prevWorld = null;
    }

    private void registerChunkTickets(@Nonnull World world) {
        ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
        ChunkManager manager = ChunkManager.getInstance((ServerWorld) world);

        prevPos = tile.getPos();
        prevWorld = world;

        for (ChunkPos chunkPos : tile.getChunkSet()) {
            chunkProvider.func_217228_a(TICKET_TYPE, chunkPos, TICKET_DISTANCE, this);
            manager.registerChunk(chunkPos, prevPos);
            chunkSet.add(chunkPos);
        }

        hasRegistered = true;
    }

    @Override
    public void tick() {
        World world = tile.getWorld();
        if (world == null || !(world.getChunkProvider() instanceof ServerChunkProvider)) {
            return;
        }
        if (!tile.isRemote()) {
            if (hasRegistered && prevWorld != null && (prevPos == null || prevWorld != world || prevPos != tile.getPos())) {
                releaseChunkTickets(prevWorld);
            }

            if (hasRegistered && !canOperate()) {
                releaseChunkTickets(world);
            }

            if (canOperate() && !hasRegistered) {
                registerChunkTickets(world);
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
        if (!tile.isRemote() && prevWorld != null) {
            releaseChunkTickets(prevWorld);
        }
    }

    /**
     * Release and re-register tickets, call when chunkset changes
     */
    public void refreshChunkTickets() {
        if (prevWorld != null) {
            releaseChunkTickets(prevWorld);
        }
        if (!tile.isRemote()) {
            registerChunkTickets(Objects.requireNonNull(tile.getWorld()));
        }
    }
}