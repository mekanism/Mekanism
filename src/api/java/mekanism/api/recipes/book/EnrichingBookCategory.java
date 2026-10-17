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
public enum EnrichingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<EnrichingBookCategory> BY_ID = ByIdMap.continuous(EnrichingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<EnrichingBookCategory> CODEC = StringRepresentable.fromEnum(EnrichingBookCategory::values);
    public static final StreamCodec<ByteBuf, EnrichingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnrichingBookCategory::ordinal);
    private final String name;

    EnrichingBookCategory(String name) {
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

    /// Recipe book info for enriching recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record EnrichingBookInfo(EnrichingBookCategory category, String group) implements Recipe.BookInfo<EnrichingBookCategory> {

        public static final MapCodec<EnrichingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, EnrichingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, EnrichingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(EnrichingBookCategory.STREAM_CODEC, EnrichingBookInfo::new);
    }
}