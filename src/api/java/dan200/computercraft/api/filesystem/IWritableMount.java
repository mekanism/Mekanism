/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.filesystem;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a part of a virtual filesystem that can be mounted onto a computer using {@link IComputerAccess#mount(String, IMount)}
 * or {@link IComputerAccess#mountWritable(String, IWritableMount)}, that can also be written to.
 *
 * Ready made implementations of this interface can be created using
 * {@link ComputerCraftAPI#createSaveDirMount(World, String, long)}, or you're free to implement it yourselves!
 *
 * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
 * @see IComputerAccess#mount(String, IMount)
 * @see IComputerAccess#mountWritable(String, IWritableMount)
 * @see IMount
 */
public interface IWritableMount extends IMount
{
    /**
     * Creates a directory at a given path inside the virtual file system.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/mynewprograms".
     * @throws IOException If the directory already exists or could not be created.
     */
    void makeDirectory( @Nonnull String path ) throws IOException;

    /**
     * Deletes a directory at a given path inside the virtual file system.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myoldprograms".
     * @throws IOException If the file does not exist or could not be deleted.
     */
    void delete( @Nonnull String path ) throws IOException;

    /**
     * Opens a file with a given path, and returns an {@link OutputStream} for writing to it.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprogram".
     * @return A stream for writing to
     * @throws IOException If the file could not be opened for writing.
     */
    @Nonnull
    OutputStream openForWrite( @Nonnull String path ) throws IOException;

    /**
     * Opens a file with a given path, and returns an {@link OutputStream} for appending to it.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprogram".
     * @return A stream for writing to.
     * @throws IOException If the file could not be opened for writing.
     */
    @Nonnull
    OutputStream openForAppend( @Nonnull String path ) throws IOException;

    /**
     * Get the amount of free space on the mount, in bytes. You should decrease this value as the user writes to the
     * mount, and write operations should fail once it reaches zero.
     *
     * @return The amount of free space, in bytes.
     * @throws IOException If the remaining space could not be computed.
     */
    long getRemainingSpace() throws IOException;
}
