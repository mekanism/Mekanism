package mekanism.common.inventory.container;

import java.util.Objects;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;

public class SelectedWindowData {

    private static final IntPredicate ONE_WINDOW = v -> v == 0;
    public static final SelectedWindowData UNSPECIFIED = new SelectedWindowData(WindowType.UNSPECIFIED);

    @Nonnull
    public final WindowType type;
    public final byte extraData;

    public SelectedWindowData(@Nonnull WindowType type) {
        this(type, (byte) 0);
    }

    /**
     * It is expected to only call this with a piece of extra data that is valid. If it is not valid this end up treating it as zero instead.
     */
    public SelectedWindowData(@Nonnull WindowType type, byte extraData) {
        this.type = Objects.requireNonNull(type);
        this.extraData = this.type.validator.test(extraData) ? extraData : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelectedWindowData other = (SelectedWindowData) o;
        return extraData == other.extraData && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, extraData);
    }

    public enum WindowType {
        CRAFTING(v -> v >= 0 && v < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS),
        UPGRADE,
        /**
         * For use by windows that don't actually have any server side specific logic required.
         */
        UNSPECIFIED;

        private final IntPredicate validator;

        WindowType() {
            this(ONE_WINDOW);
        }

        WindowType(IntPredicate validator) {
            this.validator = validator;
        }
    }
}