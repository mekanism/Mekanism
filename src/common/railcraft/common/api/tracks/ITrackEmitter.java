package railcraft.common.api.tracks;

/**
 * Tracks that can emit a redstone signal should implement
 * this interface.
 *
 * For example a detector track.
 *
 * A track cannot implement both ITrackPowered and ITrackEmitter.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ITrackEmitter extends ITrackInstance
{

    /**
     * Return true if the track is producing a redstone signal.
     *
     * @return true if powered
     */
    public boolean isTrackPowering();
}
