package mekanism.client.render.obj;

import javax.annotation.Nonnull;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class TransmitterModelTransform implements IModelTransform {

    private final boolean isUvLock;
    private final TransformationMatrix matrix;

    public TransmitterModelTransform(IModelTransform internal, Direction dir, float angle) {
        TransformationMatrix matrix = new TransformationMatrix(null, new Quaternion(vecForDirection(dir), angle, true), null, null);
        this.matrix = internal.getRotation().compose(matrix);
        this.isUvLock = internal.isUvLock();
    }

    private static Vector3f vecForDirection(Direction dir) {
        Vector3f vec = new Vector3f(Vector3d.copy(dir.getDirectionVec()));
        vec.mul(-1);
        return vec;
    }

    @Nonnull
    @Override
    public TransformationMatrix getRotation() {
        return matrix;
    }

    @Override
    public boolean isUvLock() {
        return isUvLock;
    }
}