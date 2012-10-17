
package dan200.turtle.api;
import java.lang.reflect.Method;

/**
 * The static entry point to the ComputerCraft Turtle Upgrade API.
 * Members in this class must be called after mod_CCTurtle has been initialised,
 * but may be called before it is loaded.
 */
public class TurtleAPI
{
	/**
	 * Registers a new turtle upgrade for use in the ComputerCraft. After calling this,
	 * users should be able to craft Turtles with your new upgrade. It is recommended to call
	 * this during the load() method of your mod.
	 * @throws Exception if you try to register an upgrade with an already used or reserved upgradeID
	 * @see ITurtleUpgrade
	 */
	public static void registerUpgrade( ITurtleUpgrade upgrade )
	{
		if( upgrade != null )
		{
			findCCTurtle();
			if( ccTurtle_registerTurtleUpgrade != null )
			{
				try {
					ccTurtle_registerTurtleUpgrade.invoke( null, new Object[]{ upgrade } );
				} catch( Exception e ) {
					// It failed
				}
			}
		}
	}
	
	// The functions below here are private, and are used to interface with the non-API ComputerCraft classes.
	// Reflection is used here so you can develop your mod in MCP without decompiling ComputerCraft and including
	// it in your solution.
	 
	private static void findCCTurtle()
	{
		if( !ccTurtleSearched ) {
			// Search for CCTurtle
			try {
				ccTurtle = Class.forName( "dan200.turtle.shared.CCTurtle" );
				ccTurtle_registerTurtleUpgrade = findCCTurtleMethod( "registerTurtleUpgrade", new Class[] {
					ITurtleUpgrade.class
				} );				
				
			} catch( ClassNotFoundException e ) {
				System.out.println("ComputerCraft: CCTurtle not found.");

			} finally {
				ccTurtleSearched = true;
			
			}
		}
	}
	
	private static Method findCCTurtleMethod( String name, Class[] args )
	{
		try {
			return ccTurtle.getMethod( name, args );
			
		} catch( NoSuchMethodException e ) {
			System.out.println("ComputerCraft: CCTurtle method " + name + " not found.");
			return null;
		}
	}	
	
	private static boolean ccTurtleSearched = false;	
	private static Class ccTurtle = null;
	private static Method ccTurtle_registerTurtleUpgrade = null;
}
