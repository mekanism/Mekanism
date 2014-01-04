package codechicken.multipart;

/**
 * Defines what each slot in a multipart tile corresponds to and provides some utility functions.
 * For performance reasons, it is recommended that integer constants be useds as opposed to this enum
 */
public enum PartMap
{
    BOTTOM(0),
    TOP(1),
    NORTH(2),
    SOUTH(3),
    WEST(4),
    EAST(5),
    CENTER(6),
    CORNER_NNN(7),//0
    CORNER_NPN(8),//1
    CORNER_NNP(9),//2
    CORNER_NPP(10),//3
    CORNER_PNN(11),//4
    CORNER_PPN(12),//5
    CORNER_PNP(13),//6
    CORNER_PPP(14),//7
    EDGE_NYN(15),//0
    EDGE_NYP(16),//1
    EDGE_PYN(17),//2
    EDGE_PYP(18),//3
    EDGE_NNZ(19),//4
    EDGE_PNZ(20),//5
    EDGE_NPZ(21),//6
    EDGE_PPZ(22),//7
    EDGE_XNN(23),//8
    EDGE_XPN(24),//9
    EDGE_XNP(25),//10
    EDGE_XPP(26);//11
    
    public final int i;
    public final int mask;
    
    private PartMap(int i)
    {
        this.i = i;
        mask = 1<<i;
    }

    /**
     * Don't actually use this.
     */
    public static PartMap face(int i)
    {
        return values()[i];
    }

    /**
     * Don't actually use this.
     */
    public static PartMap edge(int i)
    {
        return values()[i+15];
    }
    
    /**
     * Don't actually use this.
     */
    public static PartMap corner(int i)
    {
        return values()[i+7];
    }
    
    /**
     * Returns a 3 bit mask of the axis xzy that are variable in this edge. 
     * For example, the first 4 edges (15-18) are along the Y axis, variable in x and z, so the mask is 110b. Note axis y is 1<<0, z is 1<<1 and x is 1<<2
     * Note the parameter e is relative to the first edge slot and can range from 0-11
     */
    public static int edgeAxisMask(int e)
    {
        switch(e>>2)
        {
            case 0: return 6;
            case 1: return 5;
            case 2: return 3;
        }
        throw new IllegalArgumentException("Switch Falloff");
    }
    
    /**
     * Unpacks an edge index, to a mask where high values indicate positive positions in that axis.
     * Note the parameter e is relative to the first edge slot and can range from 0-11
     * For example, edge 1 (slot 16), is EDGE_NYP so the mask returned would be 010 as z is positive.
     */
    public static int unpackEdgeBits(int e)
    {
        switch(e>>2)
        {
            case 0: return (e&3)<<1;
            case 1: return (e&2)>>1|(e&1)<<2;
            case 2: return (e&3);
        }
        throw new IllegalArgumentException("Switch Falloff");
    }
    
    /**
     * Repacks a mask of axis bits indicating positive positions, into an edge in along the same axis as e.
     * Note the parameter e is relative to the first edge slot and can range from 0-11
     */
    public static int packEdgeBits(int e, int bits)
    {
        switch(e>>2)
        {
            case 0: return e&0xC|bits>>1;
            case 1: return e&0xC|(bits&4)>>2|(bits&1)<<1;
            case 2: return e&0xC|bits&3;
        }
        throw new IllegalArgumentException("Switch Falloff");
    }
    
    private static int[] edgeBetweenMap = new int[]{
        -1, -1, 8, 10, 4, 5,
        -1, -1, 9, 11, 6, 7,
        -1, -1,-1, -1, 0, 2,
        -1, -1,-1, -1, 1, 3};
    
    /**
     * Returns the slot of the edge between 2 sides.
     */
    public static int edgeBetween(int s1, int s2)
    {
        if(s2 < s1)
            return edgeBetween(s2, s1);
        if((s1&6) == (s2&6))
            throw new IllegalArgumentException("Faces "+s1+" and "+s2+" are opposites");
        return 15+edgeBetweenMap[s1*6+s2];
    }
}
