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
public enum LiquificationBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<LiquificationBookCategory> BY_ID = ByIdMap.continuous(LiquificationBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<LiquificationBookCategory> CODEC = StringRepresentable.fromEnum(LiquificationBookCategory::values);
    public static final StreamCodec<ByteBuf, LiquificationBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LiquificationBookCategory::ordinal);
    private final String name;

    LiquificationBookCategory(String name) {
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

    /// Recipe book info for nutritional liquification recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record LiquificationBookInfo(LiquificationBookCategory category, String group) implements Recipe.BookInfo<LiquificationBookCategory> {

        public static final MapCodec<LiquificationBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, LiquificationBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, LiquificationBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(LiquificationBookCategory.STREAM_CODEC, LiquificationBookInfo::new);
    }
}