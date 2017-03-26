package buildcraft.api.transport.pipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

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
