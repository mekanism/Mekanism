package buildcraft.api.transport.pipe_bc8.event_bc8;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

public interface IPipeEventConnection_BC8 extends IPipeEvent_BC8 {
    /** @return The connection object that is used. */
    IConnection_BC8 getConnection();

    /** @return The face that this connection is over */
    EnumFacing getFace();

    public interface AttemptCreate extends IPipeEventConnection_BC8 {
        /** Calling this will disallow ANY connection to the pipe, regardless of any previous arguments passed. */
        void disallow();

        /** Calling this any number of times will allow the connection to the pipe, but only if {@link #disallow()} has
         * not been called. If this is never called, and the connection override type is DEFAULT (As specified by
         * {@link IPipeConnection}) then there will be no connection. */
        void couldAccept();

        /** @return The connection object that will be used if this event is not blocked. */
        @Override
        IConnection_BC8 getConnection();

        Object with();

        public interface Pipe extends AttemptCreate {
            @Override
            IPipe_BC8 with();
        }

        public interface Tile extends AttemptCreate {
            @Override
            TileEntity with();
        }

        public interface MovableEntity extends AttemptCreate {
            @Override
            Entity with();
        }
    }

    /** Fired after the connection object has been created and bound in the pipe, such that
     * {@link IPipe_BC8#getConnections()} will include {@link #getFace()} mapped to {@link #getConnection()} */
    public interface Create extends IPipeEventConnection_BC8 {
        Object with();

        public interface Pipe extends Create {
            @Override
            IPipe_BC8 with();
        }

        public interface Tile extends Create {
            @Override
            TileEntity with();
        }

        public interface MovableEntity extends Create {
            @Override
            Entity with();
        }
    }

    public interface Destroy extends IPipeEventConnection_BC8 {
        Object with();

        public interface Pipe extends Destroy {
            @Override
            IPipe_BC8 with();
        }

        public interface Tile extends Destroy {
            @Override
            TileEntity with();
        }

        public interface MovableEntity extends Destroy {
            @Override
            Entity with();
        }
    }
}
