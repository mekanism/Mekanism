package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.Item;

import buildcraft.api.ITripleRegistry;

public interface IPipeRegistry extends ITripleRegistry<PipeDefinition_BC8> {
    /** Registers a definition, returning the item associated with it. Note that the item has not been registered, so
     * you still need to register it with forge. */
    Item registerDefinition(PipeDefinition_BC8 definition);
}
