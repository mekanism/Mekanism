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
import java.io.InputStream;
import java.util.List;

/**
 * Represents a read only part of a virtual filesystem that can be mounted onto a computer using
 * {@link IComputerAccess#mount(String, IMount)}
 *
 * Ready made implementations of this interface can be created using
 * {@link ComputerCraftAPI#createSaveDirMount(World, String, long)} or
 * {@link ComputerCraftAPI#createResourceMount(Class, String, String)}, or you're free to implement it yourselves!
 *
 * @see ComputerCraftAPI#createSaveDirMount(World, String, long)
 * @see ComputerCraftAPI#createResourceMount(Class, String, String)
 * @see IComputerAccess#mount(String, IMount)
 * @see IWritableMount
 */
public interface IMount
{
    /**
     * Returns whether a file with a given path exists or not.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprogram"
     * @return If the file exists.
     * @throws IOException If an error occurs when checking the existence of the file.
     */
    boolean exists( @Nonnull String path ) throws IOException;

    /**
     * Returns whether a file with a given path is a directory or not.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprograms".
     * @return If the file exists and is a directory
     * @throws IOException If an error occurs when checking whether the file is a directory.
     */
    boolean isDirectory( @Nonnull String path ) throws IOException;

    /**
     * Returns the file names of all the files in a directory.
     *
     * @param path     A file path in normalised format, relative to the mount location. ie: "programs/myprograms".
     * @param contents A list of strings. Add all the file names to this list.
     * @throws IOException If the file was not a directory, or could not be listed.
     */
    void list( @Nonnull String path, @Nonnull List<String> contents ) throws IOException;

    /**
     * Returns the size of a file with a given path, in bytes
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprogram".
     * @return The size of the file, in bytes.
     * @throws IOException If the file does not exist, or its size could not be determined.
     */
    long getSize( @Nonnull String path ) throws IOException;

    /**
     * Opens a file with a given path, and returns an {@link InputStream} representing its contents.
     *
     * @param path A file path in normalised format, relative to the mount location. ie: "programs/myprogram".
     * @return A stream representing the contents of the file.
     * @throws IOException If the file does not exist, or could not be opened.
     */
    @Nonnull
    InputStream openForRead( @Nonnull String path ) throws IOException;
}
