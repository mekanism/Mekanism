package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.client.render.outline.Outlines.Line;
import org.joml.Vector3f;

public record LineDrawer(PoseStack.Pose pose, VertexConsumer buffer, int lineColor, float lineWidth, Vector3f normal) {

    public LineDrawer(PoseStack.Pose pose, VertexConsumer buffer, int lineColor, float lineWidth) {
        this(pose, buffer, lineColor, lineWidth, new Vector3f());
    }

    public void drawLine(Line line) {
        normal.set(line.nX(), line.nY(), line.nZ()).normalize();

        buffer.addVertex(pose, line.x1(), line.y1(), line.z1()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
        buffer.addVertex(pose, line.x2(), line.y2(), line.z2()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
    }
}