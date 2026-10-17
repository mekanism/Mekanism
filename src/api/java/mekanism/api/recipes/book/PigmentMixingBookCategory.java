package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

//TODO - 26.3: Docs
public enum PigmentMixingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<PigmentMixingBookCategory> BY_ID = ByIdMap.continuous(PigmentMixingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<PigmentMixingBookCategory> CODEC = StringRepresentable.fromEnum(PigmentMixingBookCategory::values);
    public static final StreamCodec<ByteBuf, PigmentMixingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PigmentMixingBookCategory::ordinal);
    private final String name;

    PigmentMixingBookCategory(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        //TODO - 26.3: Implement this
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /// Recipe book info for pigment mixing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record PigmentMixingBookInfo(PigmentMixingBookCategory category, String group) implements Recipe.BookInfo<PigmentMixingBookCategory> {

        public static final MapCodec<PigmentMixingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, PigmentMixingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, PigmentMixingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(PigmentMixingBookCategory.STREAM_CODEC, PigmentMixingBookInfo::new);
    }
}