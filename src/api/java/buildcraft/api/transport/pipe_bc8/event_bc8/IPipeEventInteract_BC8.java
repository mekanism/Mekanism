package buildcraft.api.transport.pipe_bc8.event_bc8;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe_bc8.IPipeListener;

public interface IPipeEventInteract_BC8 extends IPipeEvent_BC8 {
    EntityPlayer getPlayer();

    /** Fired whenever a player selects a pipe. */
    public interface Select extends IPipeEventInteract_BC8 {
        /** This should be called for every {@link AxisAlignedBB} that makes up your listener, and with your listener.
         * The listener will receive the actual event directly. */
        void testBB(AxisAlignedBB aabb, IPipeListener callback);
    }

    public interface Activate extends IPipeEventInteract_BC8 {}

    public interface UseItem extends IPipeEventInteract_BC8 {
        ItemStack getItemStack();
    }

    public interface UseWrench extends IPipeEventInteract_BC8 {
        IToolWrench getWrench();
    }
}
