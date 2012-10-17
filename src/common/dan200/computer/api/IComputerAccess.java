
package dan200.computer.api;

/**
 * The interface passed to peripherals by computers or turtles, providing methods
 * that they can call. This should not be implemented by your classes. Do not interact
 * with computers except via this interface.
 */
public interface IComputerAccess
{
	/**
	 * Creates a new numbered directory in a subPath of the users game save, and return that number. To be used with mountSaveDir.<br>
	 * For example: n = createNewSaveDir( "computer/cdrom" ), will create a new
	 * numbered folder in the "computer/cdrom" subdirectory of the users save file, and return that number.
	 * mountSaveDir( "computer/rom", n ) could then be used to mount that folder onto the computers directory
	 * structure, and the value n could be saved out and used again in future to give the peripheral
	 * persistant storage.
	 * @param 	subPath		A relative file path from the users world save, where the directory should be located.
	 * @return	The numeric represenation of the name of the folder created. Will be positive.
	 * @see 	#mountSaveDir(String, String, int, boolean, long)
	 */
	public int createNewSaveDir( String subPath );
	
	/**
	 * Equivalent to mountSaveDir( String desiredLocation, String subPath, int id, boolean readOnly, long spaceLimit ) with no space limit.
	 * Mounts created with this method will have unlimited capacity.
	 * @see	#mountSaveDir(String, String, int, boolean, long)
	 */
	public String mountSaveDir( String desiredLocation, String subPath, int id, boolean readOnly );
	
	/**
	 * Mounts a directory into the computers file system, from a real directory a subPath of the users game save,
	 * with a numerical name. To be used with createNewSaveDir.<br>
	 * For example: n = createNewSaveDir( "computer/cdrom" ), will create a new
	 * numbered folder in the "computer/cdrom" subdirectory of the users save file, and return that number.
	 * mountSaveDir( "computer/rom", n ) could then be used to mount that folder onto the computers directory
	 * structure, and the value n can be saved out by the peripheral and used again, to give the peripheral
	 * persistant storage.<br>
	 * When a directory is mounted, it will appear in the computers file system, and the user will be
	 * able to use file operation to read from and write to the directory (unless readOnly, then only writes will be allowed).
	 * @param desiredLocation	The desired location in the computers file system where you would like the directory to appear.
	 *							If this location already exists, a number will be appended until a free name is found, and the
	 *							actual location will be returned. eg: "cdrom" can become "cdrom2" if two peripherals attempt to
	 *							mount "cdrom", or a "cdrom" folder already exists.
	 * @param subPath			The real relative file path from the users world save, where the directory to mount can be located.
	 * @param id				The numerical name of the folder to mount from the subPath: ex: mountSaveDir( "cdrom", "computer/cdrom", 7 )
	 *							will mount the directory "computer/cdrom/7". Use createNewSaveDir to obtain a unique directory id.
	 * @param readOnly			Whether the computer will be disallowed from making changes to the mounted directory and modifing or creating files therin.
	 * @param spaceLimit		The size limit of the mount, in bytes.
	 * @return					The location in the computers file system where the directory was mounted. This may differ from "desiredLocation", so the
	 *							return value should be kept track of so the folder can be unmounted later.
	 * @see	#createNewSaveDir(String)
	 * @see	#mountFixedDir(String, String, boolean, long)
	 * @see	#unmount(String)
	 */
	public String mountSaveDir( String desiredLocation, String subPath, int id, boolean readOnly, long spaceLimit );

	/**
	 * Equivalent to mountFixedDir( String desiredLocation, String path, boolean readOnly, long spaceLimit ) with no space limit.
	 * Mounts created with this method will have unlimited capacity.
	 * @see	#mountFixedDir(String, String, boolean, long)
	 */
	public String mountFixedDir( String desiredLocation, String path, boolean readOnly );
	
	/**
	 * Mounts a directory into the computers file system, from a real directory in the Minecraft install folder.<br>
	 * For example: mountFixedDir( "stuff", "mods/mymod/lua/stuff", true ), will mount the "lua/stuff" folder from
	 * your mod's directory into the computers filesystem at the location "stuff", with readonly permission, giving the
	 * computer access to those files.<br>
	 * When a directory is mounted, it will appear in the computers file system, and the user will be
	 * able to use file operation to read from and write to the directory (unless readOnly, then only writes will be allowed).<br>
	 * mountFixedDir can also be used to mount files, for example: mountFixedDir( "rom/apis/myapi", "mods/mymod/lua/myapi.lua", true ) can
	 * be used to have the peripheral install an API onto the computer it attaches to.
	 * @param desiredLocation	The desired location in the computers file system where you would like the directory to appear.
	 *							If this location already exists, a number will be appended until a free name is found, and the
	 *							actual location will be returned. eg: "cdrom" can become "cdrom2" if two peripherals attempt to
	 *							mount "cdrom", or a "cdrom" folder already exists.
	 * @param subPath			The real relative file path from the minecraft install root, where the directory to mount can be located.
	 * @param readOnly			Whether the computer will be disallowed from making changes to the mounted directory and modifing or creating files therin.
	 * @param spaceLimit		The size limit of the mount, in bytes.
	 * @return					The location in the computers file system where the directory was mounted. This may differ from "desiredLocation", so the
	 *							return value should be kept track of so the folder can be unmounted later.
	 * @see	#mountSaveDir(String, String, int, boolean, long)
	 * @see	#unmount(String)
	 */
	public String mountFixedDir( String desiredLocation, String path, boolean readOnly, long spaceLimit );

	/**
	 * Unmounts a directory previously mounted onto the computers file system by mountSaveDir or mountFixedDir.<br>
	 * When a directory is unmounted, it will disappear from the computers file system, and the user will no longer be able to
	 * access it. All directories mounted by a mountFixedDir or mountSaveDir are automatically unmounted when the peripheral
	 * is attached if they have not been explicitly unmounted.
	 * @param location	The desired location in the computers file system of the directory to unmount.
	 *					This must be the location of a directory previously mounted by mountFixedDir() or mountSaveDir(), as
	 *					indicated by their return value.
	 * @see	#mountSaveDir(String, String, int, boolean, long)
	 * @see	#mountFixedDir(String, String, boolean, long)
	 */
	public void unmount( String location );
	
	/**
	 * Returns the numerical ID of this computer.<br>
	 * This is the same number obtained by calling os.getComputerID() or running the "id" program from lua,
	 * and is guarunteed unique. This number will be positive.
	 * @return	The identifier.
	 */
	public int getID();	

	/**
	 * Equivalent to queueEvent( String event, Object[] arguments ) with an empty arguments array.
	 * @see	#queueEvent(String, Object[])
	 */
	public void queueEvent( String event );

	/**
	 * Causes an event to be raised on this computer, which the computer can respond to by calling
	 * os.pullEvent(). This can be used to notify the computer when things happen in the world or to
	 * this peripheral.
	 * @param event		A string identifying the type of event that has occurred, this will be
	 *					returned as the first value from os.pullEvent(). It is recommended that you
	 *					you choose a name that is unique, and recognisable as originating from your 
	 *					peripheral. eg: If your peripheral type is "button", a suitable event would be
	 *					"button_pressed".
	 * @param arguments	In addition to a name, you may pass an array of extra arguments to the event, that will
	 *					be supplied as extra return values to os.pullEvent(). Objects in the array will be converted
	 *					to lua data types in the same fashion as the return values of IPeripheral.callMethod().<br>
	 *					You may supply null to indicate that no arguments are to be supplied.
	 * @see IPeripheral#callMethod
	 */
	public void queueEvent( String event, Object[] arguments );
}
