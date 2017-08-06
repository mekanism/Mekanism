package mekanism.common.recipe.generation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.generators.common.block.states.BlockStateGenerator;
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

public class RecipeGenerator {

    // Replace calls to GameRegistry.addShapeless/ShapedRecipe with these methods, which will dump it to a json in your dir of choice
    // Also works with OD, replace GameRegistry.addRecipe(new ShapedOreRecipe/ShapelessOreRecipe with the same calls

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File RECIPE_DIR;

    public RecipeGenerator(String modid)
    {
        RECIPE_DIR = new File("recipeoutput" + File.separator + modid);
        if (!RECIPE_DIR.exists()) {
            boolean success = RECIPE_DIR.mkdirs();
            if(!success)
                throw new IllegalStateException("Cannot create recipe output dir!");
        }
    }

    public void addShapedRecipe(ItemStack result, Object... components) {
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
                if (curKey != null) {
                    Mekanism.logger.fatal("Failed to convert recipe. Provided two chars keys in a row", new IllegalArgumentException());
                    return;
                }
                curKey = (Character) o;
            } else {
                if (curKey == null) {
                    Mekanism.logger.fatal("Failed to convert recipe. Providing object without a char key", new IllegalArgumentException());
                    return;
                }
                if (o instanceof String)
                    isOreDict = true;
                try {
                    key.put(Character.toString(curKey), serializeItem(o));
                } catch (IllegalArgumentException e) {
                    Mekanism.logger.fatal("Failed to convert recipe", e);
                    return;
                }
                curKey = null;
            }
        }
        json.put("key", key);
        json.put("type", isOreDict ? "mekanism:ore_shaped" : "minecraft:crafting_shaped");
        try {
            json.put("result", serializeItem(result));
        } catch (IllegalArgumentException e) {
            Mekanism.logger.fatal("Failed to convert recipe", e);
            return;
        }

        // Add recipe_enabled condition to all available machine types.
        BlockStateMachine.MachineType machineType = BlockStateMachine.MachineType.get(result);
        if(machineType != null) {
            Map<String, String> condition = new HashMap<>();
            condition.put("type", "mekanism:recipe_enabled");
            condition.put("machineType", machineType.blockName);
            json.put("conditions", new Object[]{condition});
        }

        // Add recipe_enabled condition to generator types which can be disabled.
        BlockStateGenerator.GeneratorType generatorType = BlockStateGenerator.GeneratorType.get(result);
        if(generatorType != null && result.getItemDamage() <= BlockStateGenerator.GeneratorType.WIND_GENERATOR.meta) {
            Map<String, String> condition = new HashMap<>();
            condition.put("type", "mekanism:recipe_enabled");
            condition.put("generatorType", generatorType.blockName);
            json.put("conditions", new Object[]{condition});
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
            Mekanism.logger.fatal("Failed to write JSON", e);
        }
    }

    public void addShapelessRecipe(ItemStack result, Object... components)
    {
        Map<String, Object> json = new HashMap<>();

        boolean isOreDict = false;
        List<Map<String, Object>> ingredients = new ArrayList<>();
        for (Object o : components) {
            if (o instanceof String)
                isOreDict = true;
            try {
                ingredients.add(serializeItem(o));
            } catch (IllegalArgumentException e) {
                Mekanism.logger.fatal("Failed to convert recipe", e);
                return;
            }
        }
        json.put("ingredients", ingredients);
        json.put("type", isOreDict ? "mekanism:ore_shapeless" : "minecraft:crafting_shapeless");
        try {
            json.put("result", serializeItem(result));
        } catch (IllegalArgumentException e) {
            Mekanism.logger.fatal("Failed to convert recipe", e);
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
            Mekanism.logger.fatal("Failed to write JSON", e);
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
                        throw new IllegalArgumentException("Missing NBT mapping for Key: " + key);
                    }
                }
                ret.put("type", "mekanism:item_nbt");
                ret.put("nbt", tag);
            }

            return ret;
        }
        if (thing instanceof String) {
            String oredict = (String) thing;
            Map<String, Object> ret = new HashMap<>();

            //Filter circuits as they could be disabled -> Redirect the actual value to _constants.json with #OREDICTNAME
            if(oredict != null && (oredict.equals("circuitBasic") ||
                    oredict.equals("circuitAdvanced") ||
                    oredict.equals("circuitElite") ||
                    oredict.equals("circuitUltimate")))
            {
                ret.put("item", "#" + oredict.toUpperCase());
            }
            else
            {
                ret.put("type", "forge:ore_dict");
                ret.put("ore", oredict);
            }

            return ret;
        }

        throw new IllegalArgumentException("Not a block, item, stack, or od name");
    }
}
