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
public enum CrystallizingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<CrystallizingBookCategory> BY_ID = ByIdMap.continuous(CrystallizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CrystallizingBookCategory> CODEC = StringRepresentable.fromEnum(CrystallizingBookCategory::values);
    public static final StreamCodec<ByteBuf, CrystallizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CrystallizingBookCategory::ordinal);
    private final String name;

    CrystallizingBookCategory(String name) {
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

    /// Recipe book info for chemical crystallizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CrystallizingBookInfo(CrystallizingBookCategory category, String group) implements Recipe.BookInfo<CrystallizingBookCategory> {

        public static final MapCodec<CrystallizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CrystallizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CrystallizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CrystallizingBookCategory.STREAM_CODEC, CrystallizingBookInfo::new);
    }
}