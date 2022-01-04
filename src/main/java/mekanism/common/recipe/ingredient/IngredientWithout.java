package mekanism.common.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.common.Mekanism;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

//Slightly modified version of Tinker's IngredientWithout
// https://github.com/SlimeKnights/TinkersConstruct/blob/1.16/src/main/java/slimeknights/tconstruct/common/IngredientWithout.java
import net.minecraft.world.item.crafting.Ingredient.Value;

@MethodsReturnNonnullByDefault
public class IngredientWithout extends Ingredient {

    public static final ResourceLocation ID = Mekanism.rl("without");

    //Helper methods for the two most common types we may be creating
    public static IngredientWithout create(Tag<Item> base, ItemLike without) {
        return new IngredientWithout(Ingredient.of(base), Ingredient.of(without));
    }

    public static IngredientWithout create(Tag<Item> base, Tag<Item> without) {
        return new IngredientWithout(Ingredient.of(base), Ingredient.of(without));
    }

    private final ItemListWithout itemListWithout;

    private IngredientWithout(Ingredient base, Ingredient without) {
        this(new ItemListWithout(base, without));
    }

    private IngredientWithout(ItemListWithout itemListWithout) {
        super(Stream.of(itemListWithout));
        this.itemListWithout = itemListWithout;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return itemListWithout.base.test(stack) && !itemListWithout.without.test(stack);
    }

    @Override
    public boolean isEmpty() {
        return getItems().length == 0;
    }

    @Override
    public boolean isSimple() {
        return itemListWithout.base.isSimple() && itemListWithout.without.isSimple();
    }

    @Override
    public IIngredientSerializer<IngredientWithout> getSerializer() {
        return Serializer.INSTANCE;
    }

    private static class ItemListWithout implements Value {

        private final Ingredient base;
        private final Ingredient without;

        public ItemListWithout(Ingredient base, Ingredient without) {
            this.base = base;
            this.without = without;
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Arrays.stream(base.getItems())
                  .filter(stack -> !without.test(stack))
                  .collect(Collectors.toList());
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.TYPE, ID.toString());
            json.add(JsonConstants.BASE, base.toJson());
            json.add(JsonConstants.WITHOUT, without.toJson());
            return json;
        }
    }

    public static class Serializer implements IIngredientSerializer<IngredientWithout> {

        public static final IIngredientSerializer<IngredientWithout> INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public IngredientWithout parse(@Nonnull JsonObject json) {
            if (json.has(JsonConstants.BASE) && json.has(JsonConstants.WITHOUT)) {
                return new IngredientWithout(Ingredient.fromJson(json.getAsJsonObject(JsonConstants.BASE)),
                      Ingredient.fromJson(json.getAsJsonObject(JsonConstants.WITHOUT)));
            }
            throw new JsonParseException("A without ingredient must have both a base ingredient, and a negation ingredient.");
        }

        @Override
        public IngredientWithout parse(@Nonnull FriendlyByteBuf buffer) {
            Ingredient base = Ingredient.fromNetwork(buffer);
            Ingredient without = Ingredient.fromNetwork(buffer);
            return new IngredientWithout(base, without);
        }

        @Override
        public void write(@Nonnull FriendlyByteBuf buffer, IngredientWithout ingredient) {
            CraftingHelper.write(buffer, ingredient.itemListWithout.base);
            CraftingHelper.write(buffer, ingredient.itemListWithout.without);
        }
    }
}