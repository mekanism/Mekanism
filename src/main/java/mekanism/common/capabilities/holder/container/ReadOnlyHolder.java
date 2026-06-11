package mekanism.common.capabilities.holder.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class ReadOnlyHolder<CONTAINER> implements IContainerHolder<CONTAINER> {

    private final List<CONTAINER> containers = new ArrayList<>();

    ReadOnlyHolder() {
    }

    void addContainer(CONTAINER container) {
        containers.add(container);
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction direction) {
        //Only expose the slots if it is internal
        return direction == null ? containers : Collections.emptyList();
    }

    @Override
    public boolean canInsert(@Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        return false;
    }
}