package railcraft.common.api.signals;

/**
 * Represents a Signal state.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public enum EnumSignalAspect
{

    /**
     * The All Clear.
     */
    GREEN(0),
    /**
     * Typically means pairing in progress.
     */
    BLINK_YELLOW(1),
    /**
     * Caution, cart heading away.
     */
    YELLOW(1),
    /**
     * Maintenance warning, the signal is malfunctioning.
     */
    BLINK_RED(2),
    /**
     * Stop!
     */
    RED(2),
    /**
     * Can't happen, really it can't (or shouldn't).
     * Only used when rendering blink states (for the texture offset).
     */
    OFF(3);
    private final byte id;
    private final int textureOffset;
    private static byte nextId = 0;
    private static boolean blinkState;

    private EnumSignalAspect(int textureOffset)
    {
        this.textureOffset = textureOffset;
        id = getNextId();
    }

    /**
     * Returns the aspect id, used mainly for saving and network communication.
     * @return id
     */
    public byte getId()
    {
        return id;
    }

    /**
     * Returns the texture offset for this specific aspect.
     * @return offset
     */
    public int getTextureOffset()
    {
        return textureOffset;
    }

    /**
     * Returns true if the aspect is one of the blink states.
     * @return true if blinks
     */
    public boolean isBlinkAspect()
    {
        if(this == BLINK_YELLOW || this == BLINK_RED) {
            return true;
        }
        return false;
    }

    /**
     * Return true if the light is currently off.
     * @return true if the light is currently off.
     */
    public static boolean isBlinkOn()
    {
        return blinkState;
    }

    /**
     * Don't call this, its used to change blink states by Railcraft.
     */
    public static void invertBlinkState()
    {
        blinkState = !blinkState;
    }

    /**
     * Takes an id and returns an Aspect.
     * @param id
     * @return
     */
    public static EnumSignalAspect fromId(int id)
    {
        for(EnumSignalAspect a : EnumSignalAspect.values()) {
            if(a.getId() == id) {
                return a;
            }
        }
        return RED;
    }

    /**
     * Tests two Aspects and determines which is more restrictive.
     * The concept of "most restrictive" refers to which aspect enforces the
     * most limitations of movement to a train.
     *
     * In Railcraft the primary use is in Signal Box logic.
     *
     * @param first
     * @param second
     * @return The most restrictive Aspect
     */
    public static EnumSignalAspect mostRestrictive(EnumSignalAspect first, EnumSignalAspect second)
    {
        if(first == null && second != null) {
            return second;
        } else if(first != null && second == null) {
            return first;
        } else if(first == null && second == null) {
            return RED;
        }
        if(first == OFF || second == OFF) {
            return RED;
        }
        if(first.getId() > second.getId()) {
            return first;
        }
        return second;
    }

    private static byte getNextId()
    {
        byte i = nextId;
        nextId++;
        return i;
    }

    @Override
    public String toString()
    {
        String[] sa = name().split("_");
        String out = "";
        for(String s : sa) {
            out = out + s.substring(0, 1) + s.substring(1).toLowerCase() + " ";
        }
        out = out.trim();
        return out;
    }
}
