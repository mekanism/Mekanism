package buildcraft.api.transport.pipe_bc8;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.transport.pipe_bc8.IExtractionManager.IExtractable_BC8;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertable_BC8;

public interface IConnection_BC8 {
    Object getOther();

    /** @return The length of the connection. For connections to another buildcraft pipe or a full block this value will
     *         be 0.25. Values less than 0 are NOT permitted and returning values less than 0 will introduce crashes or
     *         errors further down! */
    double getLength();

    IExtractable_BC8 getExtractor();

    IInsertable_BC8 getInserter();

    interface Pipe extends IConnection_BC8 {
        @Override
        IPipe_BC8 getOther();
    }

    interface MovableEntity extends IConnection_BC8 {
        @Override
        Entity getOther();
    }

    interface Tile extends IConnection_BC8 {
        @Override
        TileEntity getOther();
    }
}
