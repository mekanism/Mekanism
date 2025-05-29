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
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
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
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
        JsonObject obj = JsonParser.parseString(builder.toString()).getAsJsonObject();
        if (obj.has("elements")) {
            JsonArray elements = obj.getAsJsonArray("elements");
            printoutObject(elements, df, 0, elements.size());
        } else if (obj.has("children")) {//Forge composite model
            JsonObject children = obj.getAsJsonObject("children");
            JsonArray[] childElements = new JsonArray[children.size()];
            int index = 0;
            for (Map.Entry<String, JsonElement> e : children.entrySet()) {
                JsonObject child = e.getValue().getAsJsonObject();
                JsonArray array;
                if (child.has("elements")) {
                    array = child.getAsJsonArray("elements");
                } else {
                    System.err.println("Unable to parse child: " + e.getKey());
                    array = new JsonArray();
                }
                childElements[index++] = array;
            }
            ChildData childData = ChildData.from(childElements);
            int soFar = 0;
            for (JsonArray childElement : childData.childElements) {
                soFar = printoutObject(childElement, df, soFar, childData.totalElements);
            }
        } else {
            System.err.println("Unable to parse model file.");
        }
    }

    private static int printoutObject(JsonArray elementsArray, DecimalFormat df, int soFar, int totalElements) {
        for (JsonElement jsonElement : elementsArray) {
            JsonObject element = jsonElement.getAsJsonObject();
            StringBuilder line = new StringBuilder("box(")
                  .append(convertCorner(df, element.getAsJsonArray("from")))
                  .append(", ")
                  .append(convertCorner(df, element.getAsJsonArray("to")))
                  .append(')');
            if (++soFar < totalElements) {
                //If this isn't the last element we need to make sure we have a comma at the end
                line.append(',');
            }
            if (element.has("name")) {
                line.append(" // ").append(element.get("name").getAsString());
            }
            System.out.println(line);
        }
        return soFar;
    }

    private static String convertCorner(DecimalFormat df, JsonArray corner) {
        return df.format(corner.get(0).getAsDouble()) + ", " + df.format(corner.get(1).getAsDouble()) + ", " + df.format(corner.get(2).getAsDouble());
    }

    private record ChildData(JsonArray[] childElements, int totalElements) {

        private static ChildData from(JsonArray[] childElements) {
            int elements = 0;
            for (JsonArray childElement : childElements) {
                elements += childElement.size();
            }
            return new ChildData(childElements, elements);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ChildData other = (ChildData) o;
            return totalElements == other.totalElements && Arrays.equals(childElements, other.childElements);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(childElements) + totalElements;
        }
    }
}