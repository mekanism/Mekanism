package buildcraft.api.transport.pipe_bc8;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import io.netty.buffer.ByteBuf;

/** An instance is created by instance of IBehaviourFactory per pipe block in world, and is registered with the pipe
 * event bus to listen and respond to events. */
public abstract class PipeBehaviour_BC8 implements IPipeListener {
    public final PipeDefinition_BC8 definition;
    public final IPipe_BC8 pipe;

    public PipeBehaviour_BC8(PipeDefinition_BC8 definition, IPipe_BC8 pipe) {
        if (definition == null) throw new NullPointerException("definition");
        if (pipe == null) throw new NullPointerException("pipe");
        this.definition = definition;
        this.pipe = pipe;
    }

    /** @param side The side of which the pipe should be registered
     * @return An integer between 0 (inclusive) and definition.maxSprites (exclusive). */
    public abstract int getIconIndex(EnumFacing side);

    /** Return the index for the icon for items. Override this if getIconIndex(null) does NOT return the item icon */
    public int getIconIndexForItem() {
        return getIconIndex(null);
    }

    @Override
    public abstract PipeBehaviour_BC8 readFromByteBuf(ByteBuf buf);

    @Override
    public abstract PipeBehaviour_BC8 readFromNBT(NBTBase nbt);
}
