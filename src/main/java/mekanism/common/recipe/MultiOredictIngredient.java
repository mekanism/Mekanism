package mekanism.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreIngredient;

/**
 * Created by Thiakil on 24/03/2018.
 */
@SuppressWarnings("unused")
public class MultiOredictIngredient extends CompoundIngredient {

    MultiOredictIngredient(String... ores) {
        super(getOreIngredients(ores));
    }

    private static Collection<Ingredient> getOreIngredients(String... ores) {
        if (ores == null || ores.length < 2) {
            throw new IllegalArgumentException("ores must contain at least 2 values");
        }
        List<Ingredient> children = new ArrayList<>(ores.length);
        for (String ore : ores) {
            children.add(new OreIngredient(ore));
        }
        return children;
    }

    public static class Factory implements IIngredientFactory {

        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            JsonArray oreJsonArray = JsonUtils.getJsonArray(json, "ores");
            if (oreJsonArray.size() < 2) {
                throw new JsonSyntaxException("ores must contain at least 2 values");
            }
            ArrayList<String> ores = new ArrayList<>(oreJsonArray.size());
            oreJsonArray.forEach(el -> ores.add(el.getAsString()));
            return new MultiOredictIngredient(ores.toArray(new String[0]));
        }
    }
}
