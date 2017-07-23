package buildcraft.api.mj;

import javax.annotation.Nonnull;

/** Signifies that this should visibly connect to other Mj handling entities/tiles. This should NEVER be the tile
 * entity, but an encapsulated class that refers back to it. Use {@link buildcraft.api.mj.MjAPI#CAP_CONNECTOR} to access
 * this. */
public interface IMjConnector {
    /** Checks to see if this connector can connect to the other connector. By default this should check that the other
     * connector is the same power system.
     * 
     * @param other
     * @return */
    boolean canConnect(@Nonnull IMjConnector other);
}
