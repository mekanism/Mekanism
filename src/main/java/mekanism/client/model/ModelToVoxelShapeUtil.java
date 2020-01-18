package mekanism.client.model;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.util.math.shapes.VoxelShape;

//TODO: JavaDoc - contains client side methods to help convert models to VoxelShapes
public class ModelToVoxelShapeUtil {

    public static VoxelShape getShapeFromModel(RendererModel... models) {
        return getShapeFromModel(false, models);
    }

    public static VoxelShape getShapeFromModel(boolean print, RendererModel... models) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (RendererModel model : models) {
            shapes.add(getShapeFromModel(print, model));
        }
        return VoxelShapeUtils.combine(shapes);
    }

    public static VoxelShape getShapeFromModel(boolean print, RendererModel model) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (ModelBox box : model.cubeList) {
            shapes.add(VoxelShapeUtils.getSlope(box.posX1, box.posY1, box.posZ1, box.posX2, box.posY2, box.posZ2,
                  model.rotationPointX, model.rotationPointY, model.rotationPointZ, model.rotateAngleX, model.rotateAngleY, model.rotateAngleZ, print));
        }
        if (model.childModels != null) {
            for (RendererModel childModel : model.childModels) {
                shapes.add(getShapeFromModel(print, childModel));
            }
        }
        return VoxelShapeUtils.combine(shapes, false);
    }
}