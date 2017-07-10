package buildcraft.api.recipes;

import buildcraft.api.core.IStackFilter;

/**
 * Stack definition for recipe input
 */
public final class StackDefinition {
    public final IStackFilter filter;
    public final int count;

    public StackDefinition(IStackFilter filter, int count) {
        this.filter = filter;
        this.count = count;
    }

    public StackDefinition(IStackFilter filter) {
        this(filter, 1);
    }
}
