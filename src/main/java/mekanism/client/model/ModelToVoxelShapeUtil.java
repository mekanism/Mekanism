package mekanism.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.stream.Stream;

//TODO: JavaDoc - contains client side methods to help convert models to VoxelShapes
public class ModelToVoxelShapeUtil {

    //TODO: AT to get access to the cubeList and childModel or not bother given we have already converted things
    /*public static VoxelShape getShapeFromModel(ModelRenderer... models) {
        return getShapeFromModel(false, models);
    }

    public static VoxelShape getShapeFromModel(boolean print, ModelRenderer... models) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (ModelRenderer model : models) {
            shapes.add(getShapeFromModel(print, model));
        }
        return VoxelShapeUtils.combine(shapes);
    }

    public static VoxelShape getShapeFromModel(boolean print, ModelRenderer model) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (ModelBox box : model.cubeList) {
            shapes.add(VoxelShapeUtils.getSlope(box.posX1, box.posY1, box.posZ1, box.posX2, box.posY2, box.posZ2,
                  model.rotationPointX, model.rotationPointY, model.rotationPointZ, model.rotateAngleX, model.rotateAngleY, model.rotateAngleZ, print));
        }
        if (model.childModels != null) {
            for (ModelRenderer childModel : model.childModels) {
                shapes.add(getShapeFromModel(print, childModel));
            }
        }
        return VoxelShapeUtils.combine(shapes, false);
    }*/

    public static void main(String[] args) {
        printoutModelFile("/Users/aidancbrady/Documents/Mekanism/src/main/resources/assets/mekanism/models/block/digital_miner.json");
    }

    private static void printoutModelFile(String path) {
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
            stream.forEach(s -> builder.append(s).append('\n'));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JsonObject obj = JsonParser.parseString(builder.toString()).getAsJsonObject();
        if (obj.has("elements")) {
            printoutObject(obj);
        } else if (obj.has("layers")) {
            obj.getAsJsonObject("layers").entrySet().forEach(e -> printoutObject(e.getValue().getAsJsonObject()));
        } else {
            System.err.println("Unable to parse model file.");
        }
    }

    private static void printoutObject(JsonObject obj) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
        JsonArray array = obj.getAsJsonArray("elements");
        for (int i = 0, elements = array.size(); i < elements; i++) {
            JsonObject element = array.get(i).getAsJsonObject();
            JsonElement nameObj = element.get("name");
            String name = nameObj == null ? "" : " // " + nameObj.getAsString();
            String from = convertCorner(df, element.getAsJsonArray("from"));
            String to = convertCorner(df, element.getAsJsonArray("to"));
            System.out.println("box(" + from + ", " + to + ")" + (i < elements - 1 ? "," : "") + name);
        }
    }

    private static String convertCorner(DecimalFormat df, JsonArray corner) {
        return df.format(corner.get(0).getAsDouble()) + ", " + df.format(corner.get(1).getAsDouble()) + ", " + df.format(corner.get(2).getAsDouble());
    }
}