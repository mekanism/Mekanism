package mekanism.common.base.holiday;

import java.util.Arrays;
import net.minecraft.network.chat.Component;

record HolidayMessage(Component themedLines, Component... lines) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof HolidayMessage(Component themed, Component[] content) && themedLines.equals(themed) && Arrays.equals(lines, content);
    }

    @Override
    public int hashCode() {
        return 31 * themedLines.hashCode() + Arrays.hashCode(lines);
    }
}