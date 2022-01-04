package mekanism.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

public record ModelPartData(String name, CubeListBuilder cubes, PartPose pose) {

    public ModelPartData(String name, CubeListBuilder cubes) {
        this(name, cubes, PartPose.ZERO);
    }

    public void addToDefinition(PartDefinition definition) {
        definition.addOrReplaceChild(name, cubes, pose);
    }

    public ModelPart getFromRoot(ModelPart part) {
        return part.getChild(name);
    }
}