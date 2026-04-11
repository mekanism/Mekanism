package mekanism.api;

import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public class ItemStackTemplateHelper {
    public static boolean isSameItemSameComponents(@Nullable ItemStackTemplate a, @Nullable ItemStackTemplate b) {
        if (a == null || b == null) {
            return a == null && b == null;
        } else {
            return a.is(b.item()) && a.components().equals(b.components());
        }
    }

    public static boolean matches(@Nullable ItemStackTemplate a, @Nullable ItemStackTemplate b) {
        return isSameItemSameComponents(a, b) && (a == null || a.count() == b.count());
    }

    public static int hashItemAndComponents(@Nullable ItemStackTemplate item) {
        if (item != null) {
            int result = 31 + item.typeHolder().value().hashCode();
            return 31 * result + item.components().hashCode();
        } else {
            return 0;
        }
    }
}
