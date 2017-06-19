package mekanism.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Foo {

// Replace calls to GameRegistry.addShapeless/ShapedRecipe with these methods, which will dump it to a json in your dir of choice
// Also works with OD, replace GameRegistry.addRecipe(new ShapedOreRecipe/ShapelessOreRecipe with the same calls

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File RECIPE_DIR = new File("recipeoutput");

    private static HashMap<String, String> constants = new HashMap<>();

    public static void addShapedRecipe(ItemStack result, Object... components) {
        if (!RECIPE_DIR.exists()) {
            RECIPE_DIR.mkdir();
        }

        // GameRegistry.addShapedRecipe(result, components);

        Map<String, Object> json = new HashMap<>();

        List<String> pattern = new ArrayList<>();
        int i = 0;
        while (i < components.length && components[i] instanceof String) {
            pattern.add((String) components[i]);
            i++;
        }
        json.put("pattern", pattern);

        boolean isOreDict = false;
        Map<String, Map<String, Object>> key = new HashMap<>();
        Character curKey = null;
        for (; i < components.length; i++) {
            Object o = components[i];
            if (o instanceof Character) {
                if (curKey != null)
                    throw new IllegalArgumentException("Provided two char keys in a row");
                curKey = (Character) o;
            } else {
                if (curKey == null)
                    throw new IllegalArgumentException("Providing object without a char key");
                if (o instanceof String)
                    isOreDict = true;
                try {
                    key.put(Character.toString(curKey), serializeItem(o));
                } catch (IllegalArgumentException e) {
                    System.out.println("1 Failed to convert recipe" + e.getMessage());
//                    e.printStackTrace();
                    return;
                }
                curKey = null;
            }
        }
        json.put("key", key);
        json.put("type", isOreDict ? "forge:ore_shaped" : "minecraft:crafting_shaped");
        try {
            json.put("result", serializeItem(result));
        } catch (IllegalArgumentException e) {
            System.out.println("2 Failed to convert recipe" + e.getMessage());
//            e.printStackTrace();
            return;
        }

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");

        while (f.exists()) {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");
        }

        try (FileWriter w = new FileWriter(f)) {
            GSON.toJson(json, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addShapelessRecipe(ItemStack result, Object... components)
    {
        if (!RECIPE_DIR.exists()) {
            RECIPE_DIR.mkdir();
        }

        // GameRegistry.addShapelessRecipe(result, components);

        Map<String, Object> json = new HashMap<>();

        boolean isOreDict = false;
        List<Map<String, Object>> ingredients = new ArrayList<>();
        for (Object o : components) {
            if (o instanceof String)
                isOreDict = true;
            try {
                ingredients.add(serializeItem(o));
            } catch (IllegalArgumentException e) {
                System.out.println("3 Failed to convert recipe" + e.getMessage());
//                e.printStackTrace();
                return;
            }
        }
        json.put("ingredients", ingredients);
        json.put("type", isOreDict ? "forge:ore_shapeless" : "minecraft:crafting_shapeless");
        try {
            json.put("result", serializeItem(result));
        } catch (IllegalArgumentException e) {
            System.out.println("4 Failed to convert recipe" + e.getMessage());
//            e.printStackTrace();
            return;
        }

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");

        while (f.exists()) {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");
        }


        try (FileWriter w = new FileWriter(f)) {
            GSON.toJson(json, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> serializeItem(Object thing) {
        if (thing instanceof Item) {
            return serializeItem(new ItemStack((Item) thing));
        }
        if (thing instanceof Block) {
            return serializeItem(new ItemStack((Block) thing));
        }
        if (thing instanceof ItemStack) {
            ItemStack stack = (ItemStack) thing;
            Map<String, Object> ret = new HashMap<>();
            ret.put("item", stack.getItem().getRegistryName().toString());
            if (stack.getItem().getHasSubtypes() || stack.getItemDamage() != 0) {
                ret.put("data", stack.getItemDamage());
            }
            if (stack.getCount() > 1) {
                ret.put("count", stack.getCount());
            }

            if (stack.hasTagCompound()) {
                Map<String, Object> tag = new HashMap<>();

                NBTTagCompound compound = stack.getTagCompound();
                Set<String> keySet = compound.getKeySet();

                for(String key: keySet) {

                    if(key.equals("tier") || key.equals("recipeType")) {
                        tag.put(key, compound.getInteger(key));
                    } else if (key.equals("mekData")) {
                        //TODO
                    } else {
                        System.out.println("Key: " + key);
                    }
                }

                ret.put("nbt", tag);
            }

            return ret;
        }
        if (thing instanceof String) {
            Map<String, Object> ret = new HashMap<>();

//            ret.put("item", "#" + ((String) thing)); // NOTE you need to add this to your _constants.json!
//            // todo autogenerate constants.json as well

            ret.put("type", "forge:ore_dict");
            ret.put("ore", thing);

            return ret;
        }

        throw new IllegalArgumentException("Not a block, item, stack, or od name");
    }
}
