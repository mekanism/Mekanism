package mekanism.common.lib.multiblock;

import java.util.Set;
import net.minecraft.core.Direction;

public interface IMultiblockEjector {

    void setEjectSides(Set<Direction> sides);
}