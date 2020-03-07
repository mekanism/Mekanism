package mekanism.common.tile.component;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.chunkloading.ChunkManager;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism_TileComponentChunkLoader");
    private static final TicketType<TileComponentChunkLoader<?>> TICKET_TYPE = TicketType.create("mekanism:chunk_loader", Comparator.comparing(tccl -> tccl.tile.getPos()));
    /**
     * Not 100% sure what this is, but 2 means the ticket has the same value as a forceChunk()
     */
    public static final int TICKET_DISTANCE = 2;

    /**
     * TileEntity implementing this component.
     */
    private T tile;

    private Set<ChunkPos> chunkSet = new ObjectOpenHashSet<>();

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
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().isUpgradeInstalled(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets(@Nonnull World world) {
        LOGGER.info("Attemptying to remove chunk tickets. Pos: {} World: {}", prevPos, world);
        ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
        Iterator<ChunkPos> chunkIt = chunkSet.iterator();
        ChunkManager manager = ChunkManager.getInstance((ServerWorld) world);
        while (chunkIt.hasNext()) {
            ChunkPos chunkPos = chunkIt.next();
            if (prevPos != null) {
                manager.deregisterChunk(chunkPos, prevPos);
            }
            chunkProvider.releaseTicket(TICKET_TYPE, chunkPos, TICKET_DISTANCE, this);
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
            chunkProvider.registerTicket(TICKET_TYPE, chunkPos, TICKET_DISTANCE, this);
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
        chunkSet.clear();
        ListNBT list = nbtTags.getList(NBTConstants.CHUNK_SET, NBT.TAG_LONG);
        for (INBT nbt : list) {
            chunkSet.add(new ChunkPos(((LongNBT) nbt).getLong()));
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (ChunkPos pos : chunkSet) {
            list.add(LongNBT.valueOf(pos.asLong()));
        }
        nbtTags.put(NBTConstants.CHUNK_SET, list);
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