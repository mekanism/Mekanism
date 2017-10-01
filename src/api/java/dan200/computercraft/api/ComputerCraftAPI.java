/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.api.network.IPacketNetwork;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.permissions.ITurtlePermissionProvider;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * The static entry point to the ComputerCraft API.
 * Members in this class must be called after mod_ComputerCraft has been initialised,
 * but may be called before it is fully loaded.
 */
public final class ComputerCraftAPI
{
    public static boolean isInstalled()
    {
        findCC();
        return computerCraft != null;
    }

    @Nonnull
    public static String getInstalledVersion()
    {
        findCC();
        if( computerCraft_getVersion != null )
        {
            try {
                return (String)computerCraft_getVersion.invoke( null );
            } catch (Exception e) {
                // It failed
            }
        }
        return "";
    }

    @Nonnull
    public static String getAPIVersion()
    {
        return "1.80pr1";
    }

    /**
     * Creates a numbered directory in a subfolder of the save directory for a given world, and returns that number.
     *
     * Use in conjunction with createSaveDirMount() to create a unique place for your peripherals or media items to store files.
     *
     * @param world         The world for which the save dir should be created. This should be the server side world object.
     * @param parentSubPath The folder path within the save directory where the new directory should be created. eg: "computercraft/disk"
     * @return The numerical value of the name of the new folder, or -1 if the folder could not be created for some reason.
     *
     * eg: if createUniqueNumberedSaveDir( world, "computer/disk" ) was called returns 42, then "computer/disk/42" is now
     * available for writing.
     * @see #createSaveDirMount(World, String, long)
     */
    public static int createUniqueNumberedSaveDir( @Nonnull World world, @Nonnull String parentSubPath )
    {
        findCC();
        if( computerCraft_createUniqueNumberedSaveDir != null )
        {
            try {
                return (Integer)computerCraft_createUniqueNumberedSaveDir.invoke( null, world, parentSubPath );
            } catch (Exception e) {
                // It failed
            }
        }
        return -1;
    }

    /**
     * Creates a file system mount that maps to a subfolder of the save directory for a given world, and returns it.
     *
     * Use in conjunction with IComputerAccess.mount() or IComputerAccess.mountWritable() to mount a folder from the
     * users save directory onto a computers file system.
     *
     * @param world    The world for which the save dir can be found. This should be the server side world object.
     * @param subPath  The folder path within the save directory that the mount should map to. eg: "computer/disk/42".
     *                 Use createUniqueNumberedSaveDir() to create a new numbered folder to use.
     * @param capacity The amount of data that can be stored in the directory before it fills up, in bytes.
     * @return The mount, or null if it could be created for some reason. Use IComputerAccess.mount() or IComputerAccess.mountWritable()
     * to mount this on a Computers' file system.
     * @see #createUniqueNumberedSaveDir(World, String)
     * @see IComputerAccess#mount(String, IMount)
     * @see IComputerAccess#mountWritable(String, IWritableMount)
     * @see IMount
     * @see IWritableMount
     */
    @Nullable
    public static IWritableMount createSaveDirMount( @Nonnull World world, @Nonnull String subPath, long capacity )
    {
        findCC();
        if( computerCraft_createSaveDirMount != null )
        {
            try {
                return (IWritableMount)computerCraft_createSaveDirMount.invoke( null, world, subPath, capacity );
            } catch (Exception e){
                // It failed
            }
        }
        return null;
    }

    /**
     * Creates a file system mount to a resource folder, and returns it.
     *
     * Use in conjunction with IComputerAccess.mount() or IComputerAccess.mountWritable() to mount a resource folder
     * onto a computer's file system.
     *
     * The files in this mount will be a combination of files in the specified mod jar, and resource packs that contain
     * resources with the same domain and path.
     *
     * @param modClass A class in whose jar to look first for the resources to mount. Using your main mod class is recommended. eg: MyMod.class
     * @param domain   The domain under which to look for resources. eg: "mymod".
     * @param subPath  The domain under which to look for resources. eg: "mymod/lua/myfiles".
     * @return The mount, or {@code null} if it could be created for some reason. Use IComputerAccess.mount() or
     * IComputerAccess.mountWritable() to mount this on a Computers' file system.
     * @see IComputerAccess#mount(String, IMount)
     * @see IComputerAccess#mountWritable(String, IWritableMount)
     * @see IMount
     */
    @Nullable
    public static IMount createResourceMount( @Nonnull Class<?> modClass, @Nonnull String domain, @Nonnull String subPath )
    {
        findCC();
        if( computerCraft_createResourceMount != null )
        {
            try {
                return (IMount)computerCraft_createResourceMount.invoke( null, modClass, domain, subPath );
            } catch (Exception e){
                // It failed
            }
        }
        return null;
    }

    /**
     * Registers a peripheral handler to convert blocks into {@link IPeripheral} implementations.
     *
     * @param handler The peripheral provider to register.
     * @see dan200.computercraft.api.peripheral.IPeripheral
     * @see dan200.computercraft.api.peripheral.IPeripheralProvider
     */
    public static void registerPeripheralProvider( @Nonnull IPeripheralProvider handler )
    {
        findCC();
        if ( computerCraft_registerPeripheralProvider != null)
        {
            try {
                computerCraft_registerPeripheralProvider.invoke( null, handler );
            } catch (Exception e){
                // It failed
            }
        }
    }

    /**
     * Registers a new turtle turtle for use in ComputerCraft. After calling this,
     * users should be able to craft Turtles with your new turtle. It is recommended to call
     * this during the load() method of your mod.
     *
     * @param upgrade The turtle upgrade to register.
     * @see dan200.computercraft.api.turtle.ITurtleUpgrade
     */
    public static void registerTurtleUpgrade( @Nonnull ITurtleUpgrade upgrade )
    {
        if( upgrade != null )
        {
            findCC();
            if( computerCraft_registerTurtleUpgrade != null )
            {
                try {
                    computerCraft_registerTurtleUpgrade.invoke( null, upgrade );
                } catch( Exception e ) {
                    // It failed
                }
            }
        }
    }

    /**
     * Registers a bundled redstone handler to provide bundled redstone output for blocks.
     *
     * @param handler The bundled redstone provider to register.
     * @see dan200.computercraft.api.redstone.IBundledRedstoneProvider
     */
    public static void registerBundledRedstoneProvider( @Nonnull IBundledRedstoneProvider handler )
    {
        findCC();
        if( computerCraft_registerBundledRedstoneProvider != null )
        {
            try {
                computerCraft_registerBundledRedstoneProvider.invoke( null, handler );
            } catch (Exception e) {
                // It failed
            }
        }
    }

    /**
     * If there is a Computer or Turtle at a certain position in the world, get it's bundled redstone output.
     *
     * @param world The world this block is in.
     * @param pos   The position this block is at.
     * @param side  The side to extract the bundled redstone output from.
     * @return If there is a block capable of emitting bundled redstone at the location, it's signal (0-65535) will be returned.
     * If there is no block capable of emitting bundled redstone at the location, -1 will be returned.
     * @see dan200.computercraft.api.redstone.IBundledRedstoneProvider
     */
    public static int getBundledRedstoneOutput( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side )
    {
        findCC();
        if( computerCraft_getDefaultBundledRedstoneOutput != null )
        {
            try {
                return (Integer)computerCraft_getDefaultBundledRedstoneOutput.invoke( null, world, pos, side );
            } catch (Exception e){
                // It failed
            }
        }
        return -1;
    }

    /**
     * Registers a media handler to provide {@link IMedia} implementations for Items
     *
     * @param handler The media provider to register.
     * @see dan200.computercraft.api.media.IMediaProvider
     */
    public static void registerMediaProvider( @Nonnull IMediaProvider handler )
    {
        findCC();
        if( computerCraft_registerMediaProvider != null )
        {
            try {
                computerCraft_registerMediaProvider.invoke( null, handler );
            } catch (Exception e){
                // It failed
            }
        }
    }

    /**
     * Registers a permission handler to restrict where turtles can move or build.
     *
     * @param handler The turtle permission provider to register.
     * @see dan200.computercraft.api.permissions.ITurtlePermissionProvider
     */
    public static void registerPermissionProvider( @Nonnull ITurtlePermissionProvider handler )
    {
        findCC();
        if( computerCraft_registerPermissionProvider != null )
        {
            try {
                computerCraft_registerPermissionProvider.invoke( null, handler );
            } catch (Exception e) {
                // It failed
            }
        }
    }

    public static void registerPocketUpgrade( @Nonnull IPocketUpgrade upgrade )
    {
        findCC();
        if(computerCraft_registerPocketUpgrade != null) {
            try {
                computerCraft_registerPocketUpgrade.invoke( null, upgrade );
            } catch (Exception e) {
                // It failed
            }
        }
    }

    /**
     * Attempt to get the game-wide wireless network.
     *
     * @return The global wireless network, or {@code null} if it could not be fetched.
     */
    public static IPacketNetwork getWirelessNetwork()
    {
        findCC();
        if( computerCraft_getWirelessNetwork != null )
        {
            try
            {
                return (IPacketNetwork) computerCraft_getWirelessNetwork.invoke( null );
            } catch (Exception e) {
                // It failed;
            }
        }

        return null;
    }

    // The functions below here are private, and are used to interface with the non-API ComputerCraft classes.
    // Reflection is used here so you can develop your mod without decompiling ComputerCraft and including
    // it in your solution, and so your mod won't crash if ComputerCraft is installed.

    private static void findCC()
    {
        if( !ccSearched ) {
            try {
                computerCraft = Class.forName( "dan200.computercraft.ComputerCraft" );
                computerCraft_getVersion = findCCMethod( "getVersion", new Class<?>[]{
                } );
                computerCraft_createUniqueNumberedSaveDir = findCCMethod( "createUniqueNumberedSaveDir", new Class<?>[]{
                    World.class, String.class
                } );
                computerCraft_createSaveDirMount = findCCMethod( "createSaveDirMount", new Class<?>[] {
                    World.class, String.class, Long.TYPE
                } );
                computerCraft_createResourceMount = findCCMethod( "createResourceMount", new Class<?>[] {
                    Class.class, String.class, String.class
                } );
                computerCraft_registerPeripheralProvider = findCCMethod( "registerPeripheralProvider", new Class<?>[] {
                    IPeripheralProvider.class
                } );
                computerCraft_registerTurtleUpgrade = findCCMethod( "registerTurtleUpgrade", new Class<?>[] {
                    ITurtleUpgrade.class
                } );
                computerCraft_registerBundledRedstoneProvider = findCCMethod( "registerBundledRedstoneProvider", new Class<?>[] {
                    IBundledRedstoneProvider.class
                } );
                computerCraft_getDefaultBundledRedstoneOutput = findCCMethod( "getDefaultBundledRedstoneOutput", new Class<?>[] {
                    World.class, BlockPos.class, EnumFacing.class
                } );
                computerCraft_registerMediaProvider = findCCMethod( "registerMediaProvider", new Class<?>[] {
                    IMediaProvider.class
                } );
                computerCraft_registerPermissionProvider = findCCMethod( "registerPermissionProvider", new Class<?>[] {
                    ITurtlePermissionProvider.class
                } );
                computerCraft_registerPocketUpgrade = findCCMethod( "registerPocketUpgrade", new Class<?>[] {
                    IPocketUpgrade.class
                } );
                computerCraft_getWirelessNetwork = findCCMethod( "getWirelessNetwork", new Class<?>[] {
                } );
            } catch( Exception e ) {
                System.out.println( "ComputerCraftAPI: ComputerCraft not found." );
            } finally {
                ccSearched = true;
            }
        }
    }

    private static Method findCCMethod( String name, Class<?>[] args )
    {
        try {
            if( computerCraft != null )
            {
                return computerCraft.getMethod( name, args );
            }
            return null;
        } catch( NoSuchMethodException e ) {
            System.out.println( "ComputerCraftAPI: ComputerCraft method " + name + " not found." );
            return null;
        }
    }

    private static boolean ccSearched = false;
    private static Class<?> computerCraft = null;
    private static Method computerCraft_getVersion = null;
    private static Method computerCraft_createUniqueNumberedSaveDir = null;
    private static Method computerCraft_createSaveDirMount = null;
    private static Method computerCraft_createResourceMount = null;
    private static Method computerCraft_registerPeripheralProvider = null;
    private static Method computerCraft_registerTurtleUpgrade = null;
    private static Method computerCraft_registerBundledRedstoneProvider = null;
    private static Method computerCraft_getDefaultBundledRedstoneOutput = null;
    private static Method computerCraft_registerMediaProvider = null;
    private static Method computerCraft_registerPermissionProvider = null;
    private static Method computerCraft_registerPocketUpgrade = null;
    private static Method computerCraft_getWirelessNetwork = null;
}
