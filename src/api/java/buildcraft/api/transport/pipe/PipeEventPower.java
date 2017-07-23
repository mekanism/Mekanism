package buildcraft.api.transport.pipe;

import buildcraft.api.mj.MjAPI;

public abstract class PipeEventPower extends PipeEvent {
    public final IFlowPower flow;

    protected PipeEventPower(IPipeHolder holder, IFlowPower flow) {
        super(holder);
        this.flow = flow;
    }

    protected PipeEventPower(boolean canBeCancelled, IPipeHolder holder, IFlowPower flow) {
        super(canBeCancelled, holder);
        this.flow = flow;
    }

    public static class Configure extends PipeEventPower {
        private long maxPower = 10 * MjAPI.MJ;
        /** The percentage resistance of the power pipe (as in, percentage of current power going through the pipe).
         * Should be a number between 0 and {@link MjAPI#MJ} */
        private long powerResistance = -1;
        /** The absolute loss of the power pipe. Negative numbers mean that this field will be populated from a default,
         * or by {@link #powerResistance} if it is set. This is capped at the value given in {@link #getMaxPower()} */
        private long powerLoss = -1;
        private boolean receiver = false;

        public Configure(IPipeHolder holder, IFlowPower flow) {
            super(holder, flow);
        }

        public long getMaxPower() {
            return this.maxPower;
        }

        public void setMaxPower(long maxPower) {
            this.maxPower = maxPower;
        }

        /** The absolute loss of the power pipe. Negative numbers mean that this field will be populated from a default,
         * or by {@link #powerResistance} if it is set. This is capped at the value given in {@link #getMaxPower()} */
        public long getPowerLoss() {
            return this.powerLoss;
        }

        /** The absolute loss of the power pipe. Negative numbers mean that this field will be populated from a default,
         * or by {@link #powerResistance} if it is set. This is capped at the value given in {@link #getMaxPower()} */
        public void setPowerLoss(long powerLoss) {
            this.powerLoss = powerLoss;
        }

        /** The percentage resistance of the power pipe (percentage of current power going through the pipe that will be
         * lost). Should be a number between 0 and {@link MjAPI#MJ} */
        public long getPowerResistance() {
            return this.powerResistance;
        }

        /** The percentage resistance of the power pipe (percentage of current power going through the pipe that will be
         * lost). Should be a number between 0 and {@link MjAPI#MJ} */
        public void setPowerResistance(long powerResistance) {
            this.powerResistance = powerResistance;
        }

        public boolean isReceiver() {
            return this.receiver;
        }

        /** Sets this pipe to be one that receives power from external sources. */
        public void setReceiver(boolean receiver) {
            this.receiver = receiver;
        }
    }
}
