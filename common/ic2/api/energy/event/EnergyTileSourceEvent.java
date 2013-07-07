package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergySource;

/**
 * Event announcing an energy source operation.
 *
 * This event notifies subscribers of energy sources trying to push energy into
 * an energy grid.
 *
 * The amount field indicates the maximum amount of energy left to be
 * distributed. You have to substract the amount of energy you accepted from
 * 'amount'.
 *
 * The IEnergySource posting this event has to check 'amount' to see how much
 * energy has not been used up and adjust its output buffer accordingly
 * (usually buffer -= 'initial amount' - 'amount after posting the event')
 */
public class EnergyTileSourceEvent extends EnergyTileEvent {
	/**
	 * Amount of energy provided by the energy source.
	 *
	 * amount needs to be adjusted to show the remaining unused energy.
	 */
	public int amount;
	
	public EnergyTileSourceEvent(IEnergySource energySource, int amount) {
		super(energySource);
		
		this.amount = amount;
	}
}

