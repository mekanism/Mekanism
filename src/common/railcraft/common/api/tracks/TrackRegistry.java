package railcraft.common.api.tracks;

import cpw.mods.fml.common.FMLCommonHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * The TrackRegistry is part of a system that allows 3rd party addons to simply,
 * quickly, and easily define new Tracks with unique behaviors without requiring
 * that any additional block ids be used.
 *
 * All the tracks in RailcraftProxy are implemented using this system 100%
 * (except for Gated Tracks and Switch Tracks which have some custom render code).
 *
 * To define a new track, you need to define a TrackSpec and create a ITrackInstance.
 *
 * The TrackSpec contains basic constant information about the Track, while the TrackInstace
 * controls how an individual Track block interact with the world.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 * @see TrackSpec
 * @see ITrackInstance
 * @see TrackInstanceBase
 */
public class TrackRegistry
{

    private static Map<Short, TrackSpec> trackSpecs = new HashMap<Short, TrackSpec>();

    public static void registerTrackSpec(TrackSpec trackSpec)
    {
        if(trackSpecs.put(trackSpec.getTrackId(), trackSpec) != null) {
            throw new RuntimeException("TrackId conflict detected, please adjust your config or contact the author of the " + trackSpec.getTrackTag());
        }
    }

    /**
     * Returns a cached copy of a TrackSpec object.
     *
     * @param trackId
     * @return
     */
    public static TrackSpec getTrackSpec(int trackId)
    {
        TrackSpec spec = trackSpecs.get((short)trackId);
        if(spec == null) {
            FMLCommonHandler.instance().getFMLLogger().log(Level.WARNING, "[Railcraft] Unknown Track Spec ID({0}), reverting to normal track", trackId);
            spec = trackSpecs.get(-1);
        }
        return spec;
    }

    /**
     * Returns all Registered TrackSpecs.
     * @return list of TrackSpecs
     */
    public static Map<Short, TrackSpec> getTrackSpecs()
    {
        return trackSpecs;
    }
}
