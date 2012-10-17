
package dan200.turtle.api;

/**
 * An enum representing the two different actions that an ITurtleUpgrade of type
 * Tool may be called on to perform by a turtle.
 * @see ITurtleUpgrade
 * @see ITurtleUpgrade#useTool
 */
public enum TurtleVerb
{
	/**
	 * The turtle called turtle.dig(), turtle.digUp() or turtle.digDown()
	 */
	Dig,
	
	/**
	 * The turtle called turtle.attack(), turtle.attackUp() or turtle.attackDown()
	 */
	Attack,
}
