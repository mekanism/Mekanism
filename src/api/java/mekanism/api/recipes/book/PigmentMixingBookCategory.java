package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for pigment mixing recipes.
///
/// @since 10.8.0
public enum PigmentMixingBookCategory implements MekanismBookCategory {
    MISC("misc", "pigment_mixer", null);

    public static final Codec<PigmentMixingBookCategory> CODEC = StringRepresentable.fromEnum(PigmentMixingBookCategory::values);
    public static final IntFunction<PigmentMixingBookCategory> BY_ID = ByIdMap.continuous(PigmentMixingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PigmentMixingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PigmentMixingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    PigmentMixingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    PigmentMixingBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_PIGMENT_MIXING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    @Override
    public Object getPrimaryIcon(Provider holderLookup) {
        if (this == MISC) {
            //Try to render misc as dark red pigment
            return holderLookup.get(ChemicalIds.SIMPLE_PIGMENTS.darkRed()).<Object>map(ChemicalResource::of).orElseGet(() -> new ItemStack(primaryIcon));
        }
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
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