package mekanism.common.tile.component;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.chunkloading.ChunkManager;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
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
    private static final TicketType<TileComponentChunkLoader<?>> TICKET_TYPE = TicketType.create("mekanism:chunk_loader",
          Comparator.comparing(component -> component.tile.getPos()));
    /**
     * Not 100% sure what this is, but 2 means the ticket has the same value as a forceChunk()
     */
    public static final int TICKET_DISTANCE = 2;

    /**
     * TileEntity implementing this component.
     */
    private final T tile;

    private final Set<ChunkPos> chunkSet = new ObjectOpenHashSet<>();

    @Nullable
    private World prevWorld;
    @Nullable
    private BlockPos prevPos;

    private boolean hasRegistered;
    private boolean isFirstTick = true;

    public TileComponentChunkLoader(T tile) {
        this.tile = tile;
        tile.addComponent(this);
    }


    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().isUpgradeInstalled(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets(@Nonnull World world) {
        releaseChunkTickets(world, prevPos);
    }

    private void releaseChunkTickets(@Nonnull World world, @Nullable BlockPos pos) {
        LOGGER.debug("Attempting to remove chunk tickets. Pos: {} World: {}", pos, world.getDimensionKey().getLocation());
        ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
        Iterator<ChunkPos> chunkIt = chunkSet.iterator();
        ChunkManager manager = ChunkManager.getInstance((ServerWorld) world);
        while (chunkIt.hasNext()) {
            ChunkPos chunkPos = chunkIt.next();
            if (pos != null) {
                manager.deregisterChunk(chunkPos, pos);
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
        if (world != null && !world.isRemote && world.getChunkProvider() instanceof ServerChunkProvider) {
            if (isFirstTick) {
                isFirstTick = false;
                if (!canOperate()) {
                    //If we just loaded but are not actually able to operate
                    // release any tickets we have assigned to us that we loaded with
                    releaseChunkTickets(world, tile.getPos());
                }
            }

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
    public void write(CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (ChunkPos pos : chunkSet) {
            list.add(LongNBT.valueOf(pos.asLong()));
        }
        nbtTags.put(NBTConstants.CHUNK_SET, list);
    }

    @Override
    public void invalidate() {
        if (!tile.isRemote() && prevWorld != null) {
            releaseChunkTickets(prevWorld);
        }
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {
    }

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {
    }

    /**
     * Release and re-register tickets, call when chunk set changes
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