package mekanism.api;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Docs
public class ItemStackTemplateHelper {

    //TODO - 26.1: Do we want to inline this?
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStackTemplate>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC);

    public static boolean isSameItemSameComponents(@Nullable ItemStackTemplate a, @Nullable ItemStackTemplate b) {
        if (a == null || b == null) {
            return a == null && b == null;
        } else {
            return a.is(b.item()) && a.components().equals(b.components());
        }
    }

    //TODO - 26.1: Can't this just be Objects.equals(a, b)?
    public static boolean matches(@Nullable ItemStackTemplate a, @Nullable ItemStackTemplate b) {
        return isSameItemSameComponents(a, b) && (a == null || a.count() == b.count());
    }

    //TODO - 26.1: Re-evaluate callers, I believe in general any that also hash the count could just use ItemStackTemplate#hashCode as records implement equals and hashCode natively
    // The only caveat is that if a template was created with a direct codec (I believe this would only happen if some mod is doing something very weird/incorrect)
    // then it would hash the direct holder instead of the value stored in the holder
    public static int hashItemAndComponents(@Nullable ItemStackTemplate item) {
        if (item != null) {
            int result = 31 + item.typeHolder().value().hashCode();
            return 31 * result + item.components().hashCode();
        } else {
            return 0;
        }
    }
}
