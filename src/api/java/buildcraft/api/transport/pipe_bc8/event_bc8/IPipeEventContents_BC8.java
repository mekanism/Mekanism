package buildcraft.api.transport.pipe_bc8.event_bc8;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.IPipeContents;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

/** In all events, the contents have had the appropriate variables set up as if they were in the pipe at the time the
 * event is fired. */
public interface IPipeEventContents_BC8 extends IPipeEvent_BC8 {
    IPipeContents getContents();

    // Attempts to put in some contents

    /** Checks to make sure all parts of the pipe agree to the insertion. The pipe object will automatically fire this
     * if you fire an {@link Enter} event, so most of the time you dod not actually need to check this yourself. */
    public interface AttemptEnter extends IPipeEventContents_BC8 {
        void disallow();

        /** Gets the direction that the contents came from. So if this was a pipe below, and the item was moving
         * upwards, this would be {@link EnumFacing#DOWN} */
        EnumFacing getFrom();

        /** Gets the *thing* that tried to insert to this pipe. Most of the time this will be a tile entity, however it
         * might be a robot or anything else an addon can think of. */
        Object getInserter();

        public interface Pipe extends AttemptEnter {
            @Override
            IPipe_BC8 getInserter();
        }

        /** Fired whenever a tile entity attempts to insert an item. NOTE: this will NEVER call with a pipe as the
         * argument. */
        public interface Tile extends AttemptEnter {
            @Override
            TileEntity getInserter();
        }

        public interface MovableEntity extends AttemptEnter {
            @Override
            Entity getInserter();
        }
    }

    // Actually puts in the contents

    /** Fired after {@link AttemptEnter}. The contents will be added to the pipe at some time during this event, but the
     * contents will NEVER change during the event interval. Also the contents will be the same object as when they have
     * been added to the pipe. NOTE: The pipe object will automatically fire {@link AttemptEnter} before firing this
     * event, just to check if the contents can be inserted. You do not have to manually fire {@link AttemptEnter}
     * yourself. */
    public interface Enter extends IPipeEventContents_BC8 {
        @Override
        IPipeContentsEditable getContents();

        /** Gets the direction that the contents came from. So if this was a pipe below, and the item was moving
         * upwards, this would be {@link EnumFacing#DOWN} */
        EnumFacing getFrom();

        /** Call this (while the event is firing) to make {@link #hasBeenHandled()} return true */
        void handle();

        /** @return If a listener has handled the enter event. (Usually if this returns false after an event was fired
         *         it means that something went very wrong somewhere, and the contents has NOT been used up.) If this
         *         does return false you should keep the contents to yourself. */
        boolean hasBeenHandled();
    }

    /** Fired after the contents have been removed from the pipe, but before they have been dropped onto the ground or
     * added to a different pipe or inventory. */
    public interface Exit extends IPipeEventContents_BC8 {
        @Override
        IPipeContentsEditable getContents();
    }

    public interface ChangeSpeed extends IPipeEventContents_BC8 {
        /** @return the normalized speed for this contents. For items the actual speed has been multiplied by a constant
         *         that ensures that this returns 1 if the item is going at normal speed through a cobblestone pipe, and
         *         4 if it is going at maximum speed through a gold pipe. */
        double getNormalizedSpeed();

        void setNormalizedSpeed(double speed);
    }
}
