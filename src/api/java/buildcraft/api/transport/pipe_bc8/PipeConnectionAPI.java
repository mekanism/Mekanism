package buildcraft.api.transport.pipe_bc8;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.ICustomPipeConnection;

/** Use this class to register blocks with custom block sizes so that pipes can connect to them properly. Note that you
 * do not need to register a custom pipe connection if your block implements ICustomPipeConnection. The registered
 * version also overrides your own implementation. */
public final class PipeConnectionAPI {
    private static final Map<Block, ICustomPipeConnection> connections = Maps.newHashMap();
    private static final ICustomPipeConnection NOTHING = new ICustomPipeConnection() {
        @Override
        public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
            return 0;
        }
    };

    /** Register a block with a custom connection. Useful if you don't own the block class
     * 
     * @param block The block instance
     * @param connection The connection instance */
    public static void registerConnection(Block block, ICustomPipeConnection connection) {
        connections.put(block, connection);
    }

    /** Gets the current custom connection that the */
    public static ICustomPipeConnection getCustomConnection(Block block) {
        ICustomPipeConnection connection = connections.get(block);
        if (connection != null) {
            return connection;
        }
        if (block instanceof ICustomPipeConnection) {
            return (ICustomPipeConnection) block;
        }
        return NOTHING;
    }
}
