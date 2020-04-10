package mekanism.client.render.obj;

import javax.annotation.Nonnull;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.util.Direction;

public class TransmitterModelTransform implements IModelTransform {

    private final boolean isUvLock;
    private final TransformationMatrix matrix;

    public TransmitterModelTransform(IModelTransform internal, Direction dir, float angle) {
        TransformationMatrix matrix = new TransformationMatrix(null, new Quaternion(vecForDirection(dir), angle, true), null, null);
        this.matrix = internal.getRotation().compose(matrix);
        this.isUvLock = internal.isUvLock();
    }

    private static Vector3f vecForDirection(Direction dir) {
        switch (dir) {
            case EAST:
                return Vector3f.XN;
            case WEST:
                return Vector3f.XP;
            case UP:
                return Vector3f.YN;
            case DOWN:
                return Vector3f.YP;
            case SOUTH:
                return Vector3f.ZN;
            case NORTH:
                return Vector3f.ZP;
        }
        return new Vector3f(0, 0, 0);
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