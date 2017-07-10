package buildcraft.api.transport.pipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

public abstract class PipeEventFluid extends PipeEvent {

    public final IFlowFluid flow;

    protected PipeEventFluid(IPipeHolder holder, IFlowFluid flow) {
        super(holder);
        this.flow = flow;
    }

    protected PipeEventFluid(boolean canBeCancelled, IPipeHolder holder, IFlowFluid flow) {
        super(canBeCancelled, holder);
        this.flow = flow;
    }

    public static class TryInsert extends PipeEventFluid {
        public final EnumFacing from;
        /** The incoming fluidstack. Currently changing this does nothing. */
        @Nonnull
        public final FluidStack fluid;

        public TryInsert(IPipeHolder holder, IFlowFluid flow, EnumFacing from, @Nonnull FluidStack fluid) {
            super(true, holder, flow);
            this.from = from;
            this.fluid = fluid;
        }
    }

    /** Fired after collecting the amounts of fluid that can be moved from each pipe part into the centre. */
    public static class PreMoveToCentre extends PipeEventFluid {
        /** The fluid that is being moved. Future versions of BC *might* allow more than one fluid type per pipe, but
         * for the moment the API doesn't allow pipes to do this. */
        public final FluidStack fluid;

        /** The maximum amount of fluid that the centre pipe could accept. */
        public final int totalAcceptable;

        /** Array of {@link EnumFacing#getIndex()} to the maximum amount of fluid that a given side can offer. DO NOT
         * CHANGE THIS! */
        public final int[] totalOffered;

        // Used for checking the state
        private final int[] totalOfferedCheck;

        /** Array of {@link EnumFacing#getIndex()} to the amount of fluid that the given side will actually offer to the
         * centre. This should *never* be larger than */
        public final int[] actuallyOffered;

        public PreMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int totalAcceptable, int[] totalOffered, int[] actuallyOffered) {
            super(holder, flow);
            this.fluid = fluid;
            this.totalAcceptable = totalAcceptable;
            this.totalOffered = totalOffered;
            totalOfferedCheck = Arrays.copyOf(totalOffered, totalOffered.length);
            this.actuallyOffered = actuallyOffered;
        }

        @Override
        public String checkStateForErrors() {
            for (int i = 0; i < totalOffered.length; i++) {
                if (totalOffered[i] != totalOfferedCheck[i]) {
                    return "Changed totalOffered";
                }
                if (actuallyOffered[i] > totalOffered[i]) {
                    return "actuallyOffered[" + i + "](=" + actuallyOffered[i] + ") shouldn't be greater than totalOffered[" + i + "](=" + totalOffered[i] + ")";
                }
            }
            return super.checkStateForErrors();
        }
    }

    /** Fired after {@link PreMoveToCentre} when all of the amounts have been totalled up. */
    public static class OnMoveToCentre extends PipeEventFluid {
        /** The fluid that is being moved. Future versions of BC *might* allow more than one fluid type per pipe, but
         * for the moment the API doesn't allow pipes to do this. */
        public final FluidStack fluid;

        public final int[] fluidLeavingSide;
        public final int[] fluidEnteringCentre;

        // Used for checking the state maximums
        private final int[] fluidLeaveCheck, fluidEnterCheck;

        public OnMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int[] fluidLeavingSide, int[] fluidEnteringCentre) {
            super(holder, flow);
            this.fluid = fluid;
            this.fluidLeavingSide = fluidLeavingSide;
            this.fluidEnteringCentre = fluidEnteringCentre;
            fluidLeaveCheck = Arrays.copyOf(fluidLeavingSide, fluidLeavingSide.length);
            fluidEnterCheck = Arrays.copyOf(fluidEnteringCentre, fluidEnteringCentre.length);
        }

        @Override
        public String checkStateForErrors() {
            for (int i = 0; i < fluidLeavingSide.length; i++) {
                if (fluidLeavingSide[i] > fluidLeaveCheck[i]) {
                    return "fluidLeavingSide[" + i + "](=" + fluidLeavingSide[i] + ") shouldn't be bigger than its original value!(=" + fluidLeaveCheck[i] + ")";
                }
                if (fluidEnteringCentre[i] > fluidEnterCheck[i]) {
                    return "fluidEnteringCentre[" + i + "](=" + fluidEnteringCentre[i] + ") shouldn't be bigger than its original value!(=" + fluidEnterCheck[i] + ")";
                }
                if (fluidEnteringCentre[i] > fluidLeavingSide[i]) {
                    return "fluidEnteringCentre[" + i + "](=" + fluidEnteringCentre[i] + ") shouldn't be bigger than fluidLeavingSide[" + i + "](=" + fluidLeavingSide[i] + ")";
                }
            }
            return super.checkStateForErrors();
        }
    }

    public static class SideCheck extends PipeEventFluid {
        public final FluidStack fluid;

        /** The priorities of each side. Stored inversely to the values given, so a higher priority will have a lower
         * value than a lower priority. */
        private final int[] priority = new int[6];
        private final EnumSet<EnumFacing> allowed = EnumSet.allOf(EnumFacing.class);

        public SideCheck(IPipeHolder holder, IFlowFluid flow, FluidStack fluid) {
            super(holder, flow);
            this.fluid = fluid;
        }

        /** Checks to see if a side if allowed. Note that this may return true even though a later handler might
         * disallow a side, so you should only use this to skip checking a side (for example a diamond pipe might not
         * check the filters for a specific side if its already been disallowed) */
        public boolean isAllowed(EnumFacing side) {
            return allowed.contains(side);
        }

        /** Disallows the specific side(s) from being a destination for the item. If no sides are allowed, then the
         * fluid will stay in the current pipe section. */
        public void disallow(EnumFacing... sides) {
            for (EnumFacing side : sides) {
                allowed.remove(side);
            }
        }

        public void disallowAll(Collection<EnumFacing> sides) {
            allowed.removeAll(sides);
        }

        public void disallowAllExcept(EnumFacing... sides) {
            allowed.retainAll(Lists.newArrayList(sides));
        }

        public void disallowAllExcept(Collection<EnumFacing> sides) {
            allowed.retainAll(sides);
        }

        public void disallowAll() {
            allowed.clear();
        }

        public void increasePriority(EnumFacing side) {
            increasePriority(side, 1);
        }

        public void increasePriority(EnumFacing side, int by) {
            priority[side.ordinal()] -= by;
        }

        public void decreasePriority(EnumFacing side) {
            decreasePriority(side, 1);
        }

        public void decreasePriority(EnumFacing side, int by) {
            increasePriority(side, -by);
        }

        public EnumSet<EnumFacing> getOrder() {
            if (allowed.isEmpty()) {
                return EnumSet.noneOf(EnumFacing.class);
            }
            if (allowed.size() == 1) {
                return allowed;
            }
            outer_loop: while (true) {
                int val = priority[0];
                for (int i = 1; i < priority.length; i++) {
                    if (priority[i] != val) {
                        break outer_loop;
                    }
                }
                // No need to work out the order when all destinations have the same priority
                return allowed;
            }

            int[] ordered = Arrays.copyOf(priority, 6);
            Arrays.sort(ordered);
            int last = 0;
            for (int i = 0; i < 6; i++) {
                int current = ordered[i];
                if (i != 0 && current == last) {
                    continue;
                }
                last = current;
                EnumSet<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (allowed.contains(face)) {
                        if (priority[face.ordinal()] == current) {
                            set.add(face);
                        }
                    }
                }
                if (set.size() > 0) {
                    return set;
                }
            }
            return EnumSet.noneOf(EnumFacing.class);
        }
    }
}
