package mekanism.client.render.ctm;

import static mekanism.client.render.ctm.Dir.BOTTOM;
import static mekanism.client.render.ctm.Dir.BOTTOM_LEFT;
import static mekanism.client.render.ctm.Dir.BOTTOM_RIGHT;
import static mekanism.client.render.ctm.Dir.LEFT;
import static mekanism.client.render.ctm.Dir.RIGHT;
import static mekanism.client.render.ctm.Dir.TOP;
import static mekanism.client.render.ctm.Dir.TOP_LEFT;
import static mekanism.client.render.ctm.Dir.TOP_RIGHT;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

// @formatter:off
/**
 * The CTM renderer will draw the block's FACE using by assembling 4 quadrants from the 5 available block
 * textures.  The normal Texture.png is the blocks "unconnected" texture, and is used when CTM is disabled or the block
 * has nothing to connect to.  This texture has all of the outside corner quadrants  The texture-ctm.png contains the
 * rest of the quadrants.
 * <pre><blockquote>
 * ┌─────────────────┐ ┌────────────────────────────────┐
 * │ texture.png     │ │ texture-ctm.png                │
 * │ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 16   │ 17   ║ │ │ │ 0    │ 1    │║ 2    │ 3    ║ │
 * │ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 18   │ 19   ║ │ │ │ 4    │ 5    │║ 6    │ 7    ║ │
 * │ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
 * └─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 8    │ 9    ││ 10   │ 11   │ │
 *                     │ ┼──────┼──────┼┼──────┼──────┼ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 12   │ 13   ││ 14   │ 15   │ │
 *                     │ ═══════╧═══════╗ ─────┼───── ╔ │
 *                     └────────────────────────────────┘
 * </blockquote></pre>
 * combining { 18, 13,  9, 16 }, we can generate a texture connected to the right!
 * <pre><blockquote>
 * ╔══════╤═══════
 * ║      │      │
 * ║ 16   │ 9    │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </blockquote></pre>
 *
 * combining { 18, 13, 11,  2 }, we can generate a texture, in the shape of an L (connected to the right, and up
 * <pre><blockquote>
 * ║ ─────┼───── ╚
 * ║      │      │
 * ║ 2    │ 11   │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </blockquote></pre>
 *
 * HAVE FUN!
 * -CptRageToaster-
 */
public class CTM {
	public static int REQUIRED_TEXTURES = 2;
	public static int QUADS_PER_SIDE = 4;
	
    /**
     * The Uvs for the specific "magic number" value
     */
    public static final ISubmap[] uvs = new ISubmap[]{
            //Ctm texture
            new Submap(4, 4, 0, 0),   // 0
            new Submap(4, 4, 4, 0),   // 1
            new Submap(4, 4, 8, 0),   // 2
            new Submap(4, 4, 12, 0),  // 3
            new Submap(4, 4, 0, 4),   // 4
            new Submap(4, 4, 4, 4),   // 5
            new Submap(4, 4, 8, 4),   // 6
            new Submap(4, 4, 12, 4),  // 7
            new Submap(4, 4, 0, 8),   // 8
            new Submap(4, 4, 4, 8),   // 9
            new Submap(4, 4, 8, 8),   // 10
            new Submap(4, 4, 12, 8),  // 11
            new Submap(4, 4, 0, 12),  // 12
            new Submap(4, 4, 4, 12),  // 13
            new Submap(4, 4, 8, 12),  // 14
            new Submap(4, 4, 12, 12), // 15
            // Default texture
            new Submap(8, 8, 0, 0),   // 16
            new Submap(8, 8, 8, 0),   // 17
            new Submap(8, 8, 0, 8),   // 18
            new Submap(8, 8, 8, 8)    // 19
    };
    
    public static final ISubmap FULL_TEXTURE = new Submap(16, 16, 0, 0);
    
 // @formatter:on

	/** Some hardcoded offset values for the different corner indeces */
	protected static int[] submapOffsets = { 4, 5, 1, 0 };
	/** For use via the Chisel 2 config only, altering this could cause unintended behavior */
	public static boolean disableObscuredFaceCheckConfig = false;

	public Optional<Boolean> disableObscuredFaceCheck = Optional.absent();

	protected TIntObjectMap<Dir[]> submapMap = new TIntObjectHashMap<Dir[]>();
	protected EnumMap<Dir, Boolean> connectionMap = Maps.newEnumMap(Dir.class);
	protected int[] submapCache;

	protected CTM() 
	{
		for (Dir dir : Dir.VALUES) 
		{
			connectionMap.put(dir, false);
		}

		// Mapping the different corner indeces to their respective dirs
		submapMap.put(0, new Dir[] { BOTTOM, LEFT, BOTTOM_LEFT });
		submapMap.put(1, new Dir[] { BOTTOM, RIGHT, BOTTOM_RIGHT });
		submapMap.put(2, new Dir[] { TOP, RIGHT, TOP_RIGHT });
		submapMap.put(3, new Dir[] { TOP, LEFT, TOP_LEFT });
	}

	public static CTM getInstance() {
		return new CTM();
	}

	/**
	 * @return The indeces of the typical 4x4 submap to use for the given face at the given location.
	 * 
	 *         Indeces are in counter-clockwise order starting at bottom left.
	 */
    public int[] createSubmapIndices(IBlockAccess world, BlockPos pos, EnumFacing side) {
		submapCache = new int[] { 18, 19, 17, 16 };

		if (world == null) {
            return submapCache;
        }

		buildConnectionMap(world, pos, side);

		// Map connections to submap indeces
		for (int i = 0; i < 4; i++) {
			fillSubmaps(i);
		}

		return submapCache;
	}
    
    public int[] createSubmapIndices(long data, EnumFacing side){
		submapCache = new int[] { 18, 19, 17, 16 };

		buildConnectionMap(data, side);

		// Map connections to submap indeces
		for (int i = 0; i < 4; i++) {
			fillSubmaps(i);
		}

		return submapCache;
	}
    
    public int[] getSubmapIndices() {
        return submapCache;
    }
	
    public static boolean isDefaultTexture(int id) {
        return (id == 16 || id == 17 || id == 18 || id == 19);
    }

    /**
     * Builds the connection map and stores it in this CTM instance. The {@link #connected(Dir)}, {@link #connectedAnd(Dir...)}, and {@link #connectedOr(Dir...)} methods can be used to access it.
     */
    public void buildConnectionMap(IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        for (Dir dir : Dir.VALUES) {
            connectionMap.put(dir, dir.isConnected(this, world, pos, side, state));
        }
    }
    
    public void buildConnectionMap(long data, EnumFacing side){
		for (Dir dir : Dir.VALUES){
			connectionMap.put(dir, false);
		}
		List<CTMConnections> connections = CTMConnections.decode(data);
		for (CTMConnections loc : connections){
			if (loc.getDirForSide(side) != null){
				connectionMap.put(loc.getDirForSide(side), true);
			}
		}
	}

	private void fillSubmaps(int idx) {
		Dir[] dirs = submapMap.get(idx);
		if (connectedOr(dirs[0], dirs[1])) {
			if (connectedAnd(dirs)) {
				// If all dirs are connected, we use the fully connected face,
				// the base offset value.
			    submapCache[idx] = submapOffsets[idx];
			} else {
				// This is a bit magic-y, but basically the array is ordered so
				// the first dir requires an offset of 2, and the second dir
				// requires an offset of 8, plus the initial offset for the
				// corner.
			    submapCache[idx] = submapOffsets[idx] + (connected(dirs[0]) ? 2 : 0) + (connected(dirs[1]) ? 8 : 0);
			}
		}
	}

	/**
	 * @param dir
	 *            The direction to check connection in.
	 * @return True if the cached connectionMap holds a connection in this {@link Dir direction}.
	 */
	public boolean connected(Dir dir) {
		return connectionMap.get(dir);
	}

	/**
	 * @param dirs
	 *            The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> the given {@link Dir directions}.
	 */
	public boolean connectedAnd(Dir... dirs) {
		for (Dir dir : dirs) {
			if (!connected(dir)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param dirs
	 *            The directions to check connection in.
	 * @return True if the cached connectionMap holds a connection in <i><b>one of</b></i> the given {@link Dir directions}.
	 */
	public boolean connectedOr(Dir... dirs) {
		for (Dir dir : dirs) {
			if (connected(dir)) {
				return true;
			}
		}
		return false;
    }

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     * 
     * @param world
     * @param current
     *            The position of your block.
     * @param y
     *            The position of the block to check against.
     * @param dir
     *            The {@link EnumFacing side} of the block to check for connection status. This is <i>not</i> the direction to check in.
     * @return True if the given block can connect to the given location on the given side.
     */
    public boolean isConnected(IBlockAccess world, BlockPos current, BlockPos connection, EnumFacing dir) {

        IBlockState state = world.getBlockState(current);
        return isConnected(world, current, connection, dir, state);
    }

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     * 
     * @param world
     * @param current
     *            The position of your block.
     * @param y
     *            The position of the block to check against.
     * @param dir
     *            The {@link EnumFacing side} of the block to check for connection status. This is <i>not</i> the direction to check in.
     * @param state
     *            The state to check against for connection.
     * @return True if the given block can connect to the given location on the given side.
     */
    public boolean isConnected(IBlockAccess world, BlockPos current, BlockPos connection, EnumFacing dir, IBlockState state) 
    {
        BlockPos pos2 = connection.add(dir.getDirectionVec());

        boolean disableObscured = disableObscuredFaceCheck.or(disableObscuredFaceCheckConfig);

        IBlockState obscuring = disableObscured ? null : getConnectedState(world, pos2, dir);

        boolean ret = canConnect(world, current, connection);

        // no block obscuring this face
        if(obscuring == null)
        {
            return ret;
        }

        // check that we aren't already connected outwards from this side
        ret &= !obscuring.isFullCube() || !obscuring.equals(state);

        return ret;
    }
    
    public static boolean canConnect(IBlockAccess world, BlockPos pos, BlockPos connection)
    {
    	IBlockState state = world.getBlockState(pos);
    	IBlockState con = world.getBlockState(connection);
    	
    	if(!(state.getBlock() instanceof ICTMBlock))
    	{
    		return false;
    	}
    	
    	CTMData data = ((ICTMBlock)state.getBlock()).getCTMData(state);

        // no block or a bad API user
        if(con == null || data == null) 
        {
            return false;
        }
        
        boolean ret = false;
        
        if(con.getBlock() instanceof ICTMBlock && ((ICTMBlock)con.getBlock()).getCTMData(con) != null)
        {
            String state2 = ((IStringSerializable)con.getValue(((ICTMBlock)con.getBlock()).getTypeProperty())).getName();
            
            ret = data.acceptableBlockStates.contains(state2);
        }
        
        return ret;
    }

	public static IBlockState getConnectedState(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		return state;
	}
}