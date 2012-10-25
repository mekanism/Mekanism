package railcraft.common.api.tracks;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3;

/**
 * Used by rails that modify the bounding boxes.
 *
 * For example, the Gated Rails.
 *
 * Not very useful since there is no system in place to insert custom render code.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ITrackCustomShape extends ITrackInstance
{

    public AxisAlignedBB getCollisionBoundingBoxFromPool();

    public AxisAlignedBB getSelectedBoundingBoxFromPool();

    public MovingObjectPosition collisionRayTrace(Vec3 vec3d, Vec3 vec3d1);
}
