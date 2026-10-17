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
public enum CentrifugingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<CentrifugingBookCategory> BY_ID = ByIdMap.continuous(CentrifugingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CentrifugingBookCategory> CODEC = StringRepresentable.fromEnum(CentrifugingBookCategory::values);
    public static final StreamCodec<ByteBuf, CentrifugingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CentrifugingBookCategory::ordinal);
    private final String name;

    CentrifugingBookCategory(String name) {
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

    /// Recipe book info for centrifuging recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CentrifugingBookInfo(CentrifugingBookCategory category, String group) implements Recipe.BookInfo<CentrifugingBookCategory> {

        public static final MapCodec<CentrifugingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CentrifugingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CentrifugingBookCategory.STREAM_CODEC, CentrifugingBookInfo::new);
    }
}