package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.MekanismAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Represents all the different spot for connection locations for a ctm block
 */
public enum CTMConnections 
{
    UP(Dir.TOP),
    DOWN(Dir.BOTTOM),
    NORTH(EnumFacing.EAST, Dir.RIGHT),
    SOUTH(EnumFacing.EAST, Dir.LEFT),
    EAST(Dir.RIGHT),
    WEST(Dir.LEFT),
    
    NORTH_EAST(EnumFacing.UP, Dir.TOP_RIGHT),
    NORTH_WEST(EnumFacing.UP, Dir.TOP_LEFT),
    SOUTH_EAST(EnumFacing.UP, Dir.BOTTOM_RIGHT),
    SOUTH_WEST(EnumFacing.UP, Dir.BOTTOM_LEFT),
    
    NORTH_UP(EnumFacing.EAST, Dir.TOP_RIGHT),
    NORTH_DOWN(EnumFacing.EAST, Dir.BOTTOM_RIGHT),
    SOUTH_UP(EnumFacing.EAST, Dir.TOP_LEFT),
    SOUTH_DOWN(EnumFacing.EAST, Dir.BOTTOM_LEFT),
    
    EAST_UP(Dir.TOP_RIGHT),
    EAST_DOWN(Dir.BOTTOM_RIGHT),
    WEST_UP(Dir.TOP_LEFT),
    WEST_DOWN(Dir.BOTTOM_LEFT),
    
    NORTH_EAST_UP(EnumFacing.EAST, Dir.TOP_RIGHT, true),
    NORTH_EAST_DOWN(EnumFacing.EAST, Dir.BOTTOM_RIGHT, true),
    
    SOUTH_EAST_UP(EnumFacing.EAST, Dir.TOP_LEFT, true),
    SOUTH_EAST_DOWN(EnumFacing.EAST, Dir.BOTTOM_LEFT, true),
    
    SOUTH_WEST_UP(EnumFacing.WEST, Dir.TOP_LEFT, true),
    SOUTH_WEST_DOWN(EnumFacing.WEST, Dir.BOTTOM_LEFT, true),
    
    NORTH_WEST_UP(EnumFacing.WEST, Dir.TOP_RIGHT, true),
    NORTH_WEST_DOWN(EnumFacing.WEST, Dir.BOTTOM_RIGHT, true),
    
    UP_UP(EnumFacing.UP, null, true),
    DOWN_DOWN(EnumFacing.DOWN, null, true),
    NORTH_NORTH(EnumFacing.NORTH, null, true),
    SOUTH_SOUTH(EnumFacing.SOUTH, null, true),
    EAST_EAST(EnumFacing.EAST, null, true),
    WEST_WEST(EnumFacing.WEST, null, true);
    
    public static final CTMConnections[] VALUES = values();
    
    /**
     * The enum facing directions needed to get to this connection location
     */
    private EnumFacing normal;
    private Dir dir;
    private boolean offset;

    private CTMConnections(Dir dir) 
    {
        this(EnumFacing.SOUTH, dir);
    }
    
    private CTMConnections(Dir dir, boolean offset) 
    {
        this(EnumFacing.SOUTH, dir, offset);
    }
    
    private CTMConnections(EnumFacing normal, Dir dir)
    {
        this(normal, dir, false);
    }
    
    private CTMConnections(EnumFacing normal, Dir dir, boolean offset) 
    {
        this.normal = normal;
        this.dir = dir;
        this.offset = offset;
    }

    public Dir getDirForSide(EnumFacing facing)
    {
        return dir == null ? null : dir;//TODO .relativize()
    }

    public EnumFacing clipOrDestroy(EnumFacing direction) 
    {
        EnumFacing[] dirs = dir == null ? new EnumFacing[] {normal, normal} : dir.getNormalizedDirs(direction);
        
        if(dirs[0] == direction) 
        {
            return dirs.length > 1 ? dirs[1] : null;
        } 
        else if(dirs.length > 1 && dirs[1] == direction) 
        {
            return dirs[0];
        } 
        else {
            return null;
        }
    }

    public BlockPos transform(BlockPos pos) 
    {
        if(dir != null) 
        {
            for(EnumFacing facing : dir.getNormalizedDirs(normal))
            {
                pos = pos.offset(facing);
            }
        } 
        else {
            pos = pos.offset(normal);
        }

        if(offset) 
        {
            pos = pos.offset(normal);
        }
        
        return pos;
    }

    public static CTMConnections fromFacing(EnumFacing facing)
    {
        switch(facing)
        {
            case NORTH: return NORTH;
            case SOUTH: return SOUTH;
            case EAST: return EAST;
            case WEST: return WEST;
            case UP: return UP;
            case DOWN: return DOWN;
            default: return NORTH;
        }
    }

    public static EnumFacing toFacing(CTMConnections loc)
    {
        switch(loc)
        {
            case NORTH: return EnumFacing.NORTH;
            case SOUTH: return EnumFacing.SOUTH;
            case EAST: return EnumFacing.EAST;
            case WEST: return EnumFacing.WEST;
            case UP: return EnumFacing.UP;
            case DOWN: return EnumFacing.DOWN;
            default: return EnumFacing.NORTH;
        }
    }

    public static List<CTMConnections> decode(long data)
    {
        List<CTMConnections> list = new ArrayList<>();
        
        for(CTMConnections loc : values())
        {
            if((1 & (data >> loc.ordinal())) != 0) 
            {
                list.add(loc);
            }
        }
        
        return list;
    }

    public long getMask()
    {
        return 1 << ordinal();
    }

    public static List<CTMConnections> getConnections(IBlockAccess world, BlockPos pos, CTMConnections[] values)
    {
        List<CTMConnections> locs = new ArrayList<>();
        
        IBlockState state = world.getBlockState(pos);
        
        for(CTMConnections loc : values) 
        {
            BlockPos second = loc.transform(pos);
            
            if(CTM.canConnect(world, pos, second))
            {
                locs.add(loc);
            }
        }
        
        return locs;
    }

    public static long getData(IBlockAccess world, BlockPos pos, CTMConnections[] values)
    {
        List<CTMConnections> locs = getConnections(world, pos, values);
        
        long data = 0;
        
        for(CTMConnections loc : locs)
        {
            data = data | loc.getMask();
        }
        
        if(MekanismAPI.debug) 
        {
            String s = Long.toBinaryString(data);
            
            while(s.length() < 32)
            {
                s = "0" + s;
            }
            
            System.out.println(pos + ": " + s);
        }
        
        return data;
    }
}