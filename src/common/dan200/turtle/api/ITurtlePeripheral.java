
package dan200.turtle.api;
import dan200.computer.api.IPeripheral;

/**
 * A subclass of IPeripheral specifically for peripherals
 * created by ITurtleUpgrade's of type Peripheral. When an
 * ITurtlePeripheral is created, its IPeripheral methods will be called
 * just as if the peripheral was a seperate adjacent block in the world,
 * and update() will be called once per tick.
 * @see ITurtleUpgrade
 */
public interface ITurtlePeripheral extends IPeripheral
{
	/**
	 * A method called on each created turtle peripheral once per tick,
	 * over the lifetime of the turtle. May be used to update the state 
	 * of the peripheral, and may interact with IComputerAccess or ITurtleAccess
	 * however it likes at this time.
	 */
	public void update();
}
