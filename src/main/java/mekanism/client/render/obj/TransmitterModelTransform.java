package mekanism.client.render.obj;

import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class TransmitterModelTransform implements ModelState {

    private final boolean isUvLock;
    private final Transformation matrix;

    public TransmitterModelTransform(ModelState internal, Direction dir, float angle) {
        Transformation matrix = new Transformation(null, new Quaternion(vecForDirection(dir), angle, true), null, null);
        this.matrix = internal.getRotation().compose(matrix);
        this.isUvLock = internal.isUvLocked();
    }

    private static Vector3f vecForDirection(Direction dir) {
        Vector3f vec = new Vector3f(Vec3.atLowerCornerOf(dir.getNormal()));
        vec.mul(-1);
        return vec;
    }

    @Nonnull
    @Override
    public Transformation getRotation() {
        return matrix;
    }

    @Override
    public boolean isUvLocked() {
        return isUvLock;
    }
}