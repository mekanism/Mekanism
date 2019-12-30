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

    public TransmitterModelTransform(IModelTransform internal, Direction dir) {
        TransformationMatrix matrix = new TransformationMatrix(null, new Quaternion(vecForDirection(dir), 90, true), null, null);
        this.matrix = internal.func_225615_b_().func_227985_a_(matrix);
        this.isUvLock = internal.isUvLock();
    }

    private static Vector3f vecForDirection(Direction dir) {
        switch (dir) {
            case EAST:
                return Vector3f.field_229178_a_;
            case WEST:
                return Vector3f.field_229179_b_;
            case UP:
                return Vector3f.field_229180_c_;
            case DOWN:
                return Vector3f.field_229181_d_;
            case SOUTH:
                return Vector3f.field_229182_e_;
            case NORTH:
                return Vector3f.field_229183_f_;
        }
        return new Vector3f(0, 0, 0);
    }

    @Nonnull
    @Override
    public TransformationMatrix func_225615_b_() {
        return matrix;
    }

    @Override
    public boolean isUvLock() {
        return isUvLock;
    }
}