package ic2.api.energy;

public class NodeStats {
	public NodeStats(double energyIn, double energyOut, double voltage) {
		this.energyIn = energyIn;
		this.energyOut = energyOut;
		this.voltage = voltage;
	}

	public double getEnergyIn() {
		return energyIn;
	}

	public double getEnergyOut() {
		return energyOut;
	}

	public double getVoltage() {
		return voltage;
	}

	private final double energyIn;
	private final double energyOut;
	private final double voltage;
}
