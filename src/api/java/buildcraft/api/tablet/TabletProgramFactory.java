package buildcraft.api.tablet;

public abstract class TabletProgramFactory {
    /** Create an instance of the tablet program specified by this Factory.
     *
     * Both parameters are mutable and can be edited freely; however, the NBTTagCompound will only be synchronized after
     * you leave the program.
     *
     * Please note that the program runs client-side SOLELY. For server-side queries, you must implement a custom
     * communications protocol.
     * 
     * @param tablet
     * @return An instance of the TabletProgram. */
    public abstract TabletProgram create(ITablet tablet);

    public abstract String getName();

    public abstract TabletBitmap getIcon();
}
