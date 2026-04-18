package mekanism.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

public class ItemStackTemplateHelper {

    public static final MapCodec<ItemStackTemplate> NO_COUNT_MAPCODEC = RecordCodecBuilder.mapCodec(
          i -> i.group(
                      Item.CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
                      DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components)
                )
                .apply(i, ItemStackTemplate::new));
    public static final Codec<ItemStackTemplate> NO_COUNT_CODEC = NO_COUNT_MAPCODEC.codec();

    //TODO 26.1 These should probably be moved to ItemStackTemplate + NO_COUNT_CODEC ?
    public static final Codec<ItemStack> NO_COUNT_ITEMSTACK = RecordCodecBuilder.create(
          i -> i.group(
                      Item.CODEC.fieldOf("id").forGetter(ItemStack::typeHolder),
                      DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter((ItemStack stack) -> ((PatchedDataComponentMap) stack.getComponents()).asPatch()
                      ))
                .apply(i, (item, patch) -> new ItemStack(item, 1, patch)));

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
