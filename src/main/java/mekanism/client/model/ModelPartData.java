package mekanism.client.model;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

public record ModelPartData(String name, CubeListBuilder cubes, PartPose pose, List<ModelPartData> children) {

    public ModelPartData(String name, CubeListBuilder cubes, PartPose pose, ModelPartData... children) {
        this(name, cubes, pose, List.of(children));
    }

    public ModelPartData(String name, CubeListBuilder cubes, ModelPartData... children) {
        this(name, cubes, PartPose.ZERO, children);
    }

    public void addToDefinition(PartDefinition definition) {
        PartDefinition subDefinition = definition.addOrReplaceChild(name, cubes, pose);
        for (ModelPartData child : children) {
            child.addToDefinition(subDefinition);
        }
    }

    public ModelPart getFromRoot(ModelPart part) {
        return part.getChild(name);
    }
}