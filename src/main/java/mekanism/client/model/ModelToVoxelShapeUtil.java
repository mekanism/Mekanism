package mekanism.client.model;

//TODO: JavaDoc - contains client side methods to help convert models to VoxelShapes
public class ModelToVoxelShapeUtil {

    //TODO: 1.15 AT to get access to the cubeList and childModel or not bother given we have already converted things
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
}