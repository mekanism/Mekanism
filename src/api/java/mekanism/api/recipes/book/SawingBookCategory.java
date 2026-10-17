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
public enum SawingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<SawingBookCategory> BY_ID = ByIdMap.continuous(SawingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<SawingBookCategory> CODEC = StringRepresentable.fromEnum(SawingBookCategory::values);
    public static final StreamCodec<ByteBuf, SawingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SawingBookCategory::ordinal);
    private final String name;

    SawingBookCategory(String name) {
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

    /// Recipe book info for sawing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record SawingBookInfo(SawingBookCategory category, String group) implements Recipe.BookInfo<SawingBookCategory> {

        public static final MapCodec<SawingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, SawingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, SawingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(SawingBookCategory.STREAM_CODEC, SawingBookInfo::new);
    }
}