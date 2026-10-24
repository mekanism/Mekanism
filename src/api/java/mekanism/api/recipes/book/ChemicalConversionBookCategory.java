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
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for chemical conversion recipes.
///
/// @since 10.8.0
public enum ChemicalConversionBookCategory implements MekanismBookCategory {
    MISC("misc", BlockItemIds.RED_MUSHROOM.item(), MekanismBookCategory.createMekanismKey("enriched_diamond"));

    public static final Codec<ChemicalConversionBookCategory> CODEC = StringRepresentable.fromEnum(ChemicalConversionBookCategory::values);
    public static final IntFunction<ChemicalConversionBookCategory> BY_ID = ByIdMap.continuous(ChemicalConversionBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ChemicalConversionBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalConversionBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    ChemicalConversionBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    ChemicalConversionBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, this.name);
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
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        if (this == MISC) {
            //Try to render misc as diamond
            Optional<Object> result = holderLookup.get(ChemicalIds.DIAMOND).map(ChemicalResource::of);
            if (result.isPresent()) {
                return result;
            }
            //Fall through to fallback icon
        }
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for chemical conversion recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record ChemicalConversionBookInfo(ChemicalConversionBookCategory category, String group) implements Recipe.BookInfo<ChemicalConversionBookCategory> {

        public static final MapCodec<ChemicalConversionBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, ChemicalConversionBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalConversionBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(ChemicalConversionBookCategory.STREAM_CODEC, ChemicalConversionBookInfo::new);
    }
}