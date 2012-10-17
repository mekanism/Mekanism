
package dan200.turtle.api;

/**
 * The primary interface for defining an upgrade for Turtles. A turtle upgrade
 * can either be a new tool, or a new peripheral.
 * @see TurtleAPI#registerUpgrade( ITurtleUpgrade )
 */
public interface ITurtleUpgrade
{
	/**
	 * Gets a unique numerical identifier representing this type of turtle upgrade.
	 * Like Minecraft block and item IDs, you should strive to make this number unique
	 * among all turtle upgrades that have been released for ComputerCraft.
	 * The ID must be in the range 64 to 255, as the ID is stored as an 8-bit value,
	 * and 0-64 is reserved for future use by ComputerCraft. The upgrade will
	 * fail registration if an already used ID is specified.
	 * @see TurtleAPI#registerUpgrade( ITurtleUpgrade )
	 */
	public int getUpgradeID();
	
	/**
	 * Return a String to describe this type of upgrade in turtle item names.
	 * Examples of built-in adjectives are "Wireless", "Mining" and "Crafty".
	 */	
	public String getAdjective();

	/**
	 * Return whether this upgrade adds a tool or a peripheral to the turtle.
	 * Currently, turtle crafting is restricted to one tool & one peripheral per turtle.
	 * @see TurtleUpgradeType for the differences between the two.
	 */	
	public TurtleUpgradeType getType();
	
	/**
	 * Return an item stack representing the type of item that a turtle must be crafted
	 * with to create a turtle which holds this upgrade.
	 * Currently, turtle crafting is restricted to one tool & one peripheral per turtle.
	 */		
	public net.minecraft.src.ItemStack getCraftingItem();

	/**
	 * Return whether this turtle upgrade is an easter egg, and should be attempted to be hidden
	 * from the creative mode inventory and recipe book plugins.
	 */		
	public boolean isSecret();
	
	/**
	 * Return which texture file should be used to render this upgrade when rendering a turtle
	 * in the world or as an item.
	 * @param turtle Access to the turtle that is being rendered, this will be null when
	 * the method is being called to render the turtle as an Item. For turtles in the world,
	 * this can be used to have the upgrade change appearance based on state.
	 * @param side Which side of the turtle (left or right) that the upgrade being rendered resides on.
	 * @see #getIconIndex
	 */		
	public String getIconTexture( ITurtleAccess turtle, TurtleSide side );
	
	/**
	 * Return which icon index should be used to render this upgrade when rendering a turtle
	 * in the world or as an item.
	 * @param turtle Access to the turtle that is being rendered, this will be null when
	 * the method is being called to render the turtle as an Item. For turtles in the world,
	 * this can be used to have the upgrade change appearance based on state.
	 * @param side Which side of the turtle (left or right) that the upgrade being rendered resides on.
	 * @see #getIconTexture
	 */		
	public int getIconIndex( ITurtleAccess turtle, TurtleSide side );
	
	/**
	 * Will only be called for Peripheral upgrades. Creates a peripheral for a turtle
	 * being placed using this upgrade. The peripheral created will be stored
	 * for the lifetime of the turtle, will have update() called once-per-tick, and will be
	 * attach'd detach'd and have methods called in the same manner as a Computer peripheral.
	 * @param turtle Access to the turtle that the peripheral is being created for.
	 * @param side Which side of the turtle (left or right) that the upgrade resides on.
	 * @returns The newly created peripheral. You may return null if this upgrade is a Tool
	 * and this method is not expected to be called.
	 */		
	public ITurtlePeripheral createPeripheral( ITurtleAccess turtle, TurtleSide side );

	/**
	 * Will only be called for Tool upgrades. Called when turtle.dig() or turtle.attack() is called
	 * by the turtle, and the tool is required to do some work.
	 * @param turtle Access to the turtle that the tool resides on.
	 * @param side Which side of the turtle (left or right) the tool resides on.
	 * @param verb Which action (dig or attack) the turtle is being called on to perform.
	 * @param direction Which world direction the action should be performed in, relative to the turtles
	 * position. This will either be up, down, or the direction the turtle is facing, depending on
	 * whether dig, digUp or digDown was called.
	 * @return Whether the turtle was able to perform the action, and hence whether the turtle.dig()
	 * or turtle.attack() lua method should return true. If true is returned, the tool will perform
	 * a swinging animation. You may return false if this upgrade is a Peripheral
	 * and this method is not expected to be called.
	 */
	public boolean useTool( ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction );
}
