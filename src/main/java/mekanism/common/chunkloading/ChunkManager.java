//TODO: Chunk Loading
/*package mekanism.common.chunkloading;

import com.google.common.collect.ListMultimap;
import java.util.List;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.Ticket;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.PlayerOrderedLoadingCallback;

public class ChunkManager implements LoadingCallback, PlayerOrderedLoadingCallback {

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        //net.minecraftforge.common.ticket
        for (Ticket ticket : tickets) {
            int x = ticket.getModData().getInt("x");
            int y = ticket.getModData().getInt("y");
            int z = ticket.getModData().getInt("z");
            TileEntity tile = MekanismUtils.getTileEntity(world, x, y, z);
            if (tile instanceof IChunkLoader) {
                TileComponentChunkLoader chunkLoader = ((IChunkLoader) tile).getChunkLoader();
                chunkLoader.refreshChunkSet();
                chunkLoader.forceChunks(ticket);
            }
        }
    }

    @Override
    public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world) {
        return tickets;
    }
}*/