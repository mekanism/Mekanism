package buildcraft.api.mj;

/** Indicates an MJ consumer that has readable information. */
public interface IMjReadable extends IMjConnector {
    long getStored();

    long getCapacity();
}
