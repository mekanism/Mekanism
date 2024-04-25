package mekanism.common.inventory.container;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectedWindowData {

    public static final SelectedWindowData UNSPECIFIED = new SelectedWindowData(WindowType.UNSPECIFIED);

    @NotNull
    public final WindowType type;
    public final byte extraData;

    public SelectedWindowData(@NotNull WindowType type) {
        this(type, (byte) 0);
    }

    /**
     * It is expected to only call this with a piece of extra data that is valid. If it is not valid this end up treating it as zero instead.
     */
    public SelectedWindowData(@NotNull WindowType type, byte extraData) {
        this.type = Objects.requireNonNull(type);
        this.extraData = this.type.isValid(extraData) ? extraData : 0;
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

    /**
     * @apiNote Only call this on the client.
     */
    public void updateLastPosition(int x, int y, boolean pinned) {
        String saveName = type.getSaveName(extraData);
        if (saveName != null) {
            CachedWindowPosition cachedPosition = MekanismConfig.client.lastWindowPositions.get(saveName);
            if (cachedPosition != null) {
                boolean changed = false;
                CachedIntValue cachedX = cachedPosition.x();
                if (cachedX.get() != x) {
                    cachedX.set(x);
                    changed = true;
                }
                CachedIntValue cachedY = cachedPosition.y();
                if (cachedY.get() != y) {
                    cachedY.set(y);
                    changed = true;
                }
                CachedBooleanValue cachedPinned = cachedPosition.pinned;
                if (cachedPinned != null && cachedPinned.get() != pinned) {
                    cachedPinned.set(pinned);
                    changed = true;
                }
                if (changed) {
                    MekanismConfig.client.save();
                }
            }
        }
    }

    public boolean wasPinned() {
        return getLastPosition().pinned();
    }

    /**
     * @apiNote Only call this on the client.
     */
    public WindowPosition getLastPosition() {
        String saveName = type.getSaveName(extraData);
        if (saveName != null) {
            CachedWindowPosition cachedPosition = MekanismConfig.client.lastWindowPositions.get(saveName);
            if (cachedPosition != null) {
                return new WindowPosition(cachedPosition.x().get(), cachedPosition.y().get(), cachedPosition.pinned() != null && cachedPosition.pinned().get());
            }
        }
        return new WindowPosition(Integer.MAX_VALUE, Integer.MAX_VALUE, false);
    }

    public record CachedWindowPosition(CachedIntValue x, CachedIntValue y, @Nullable CachedBooleanValue pinned) {
    }

    public record WindowPosition(int x, int y, boolean pinned) {
    }

    public enum WindowType {
        COLOR("color", false),
        CONFIRMATION("confirmation", false),
        CRAFTING("crafting", true, IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS),
        MEKA_SUIT_HELMET("mekaSuitHelmet", false),
        RENAME("rename", false),
        SKIN_SELECT("skinSelect", false),
        SIDE_CONFIG("sideConfig", true),
        TRANSPORTER_CONFIG("transporterConfig", true),
        UPGRADE("upgrade", true),
        /**
         * For use by windows that don't actually have any server side specific logic required, or don't persist their position.
         */
        UNSPECIFIED(null, false);

        public static final IntFunction<WindowType> BY_ID = ByIdMap.continuous(WindowType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, WindowType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, WindowType::ordinal);

        @Nullable
        private final String saveName;
        private final boolean canPin;
        private final byte maxData;

        WindowType(@Nullable String saveName, boolean canPin) {
            this(saveName, canPin, (byte) 1);
        }

        WindowType(@Nullable String saveName, boolean canPin, byte maxData) {
            this.saveName = saveName;
            this.maxData = maxData;
            this.canPin = canPin;
        }

        @Nullable
        String getSaveName(byte extraData) {
            return maxData == 1 ? saveName : saveName + extraData;
        }

        public List<String> getSavePaths() {
            if (saveName == null) {
                return Collections.emptyList();
            } else if (maxData == 1) {
                return Collections.singletonList(saveName);
            }
            List<String> savePaths = new ArrayList<>();
            for (int i = 0; i < maxData; i++) {
                savePaths.add(saveName + i);
            }
            return savePaths;
        }

        public boolean canPin() {
            return canPin;
        }

        public boolean isValid(byte extraData) {
            return extraData >= 0 && extraData < maxData;
        }
    }
}