package ic2.api.util;

public enum FluidContainerOutputMode {
	EmptyFullToOutput(true), // move only completely filled/drained containers to output
	AnyToOutput(true), // move any containers to output if they changed
	InPlacePreferred(false), // adjust the input stack if possible (size == 1)
	InPlace(false); // always adjust the input stack

	private FluidContainerOutputMode(boolean outputEmptyFull) {
		this.outputEmptyFull = outputEmptyFull;
	}

	public boolean isOutputEmptyFull() {
		return outputEmptyFull;
	}

	private final boolean outputEmptyFull;
}