package mekanism.api;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public class ItemStackTemplateHelper {

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStackTemplate>> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Optional<ItemStackTemplate> decode(RegistryFriendlyByteBuf input) {
            boolean present = ByteBufCodecs.BOOL.decode(input);
            if (present) {
                return Optional.of(ItemStackTemplate.STREAM_CODEC.decode(input));
            }
            return Optional.empty();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf output, Optional<ItemStackTemplate> value) {
            if (value.isPresent()) {
                ByteBufCodecs.BOOL.encode(output, true);
                ItemStackTemplate.STREAM_CODEC.encode(output, value.get());
            } else {
                ByteBufCodecs.BOOL.encode(output, false);
            }
        }
    };

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
