package mekanism.api;

/**
 * @since 10.4.0
 */
public interface SupportsColorMap {

    /**
     * Gets the 0-1 of this color's RGB value by dividing by 255 (used for OpenGL coloring).
     *
     * @param index - R:0, G:1, B:2
     *
     * @return the color value
     */
    default float getColor(int index) {
        return getRgbCode()[index] / 255F;
    }

    /**
     * Gets the red, green and blue color value, as an integer(range: 0 - 255).
     *
     * @return the color values.
     *
     * @apiNote Modifying the returned array will result in this color object changing the color it represents, and should not be done.
     */
    int[] getRgbCode();

    /**
     * Gets the red, green and blue color value, as a float(range: 0 - 1).
     *
     * @return the color values.
     */
    default float[] getRgbCodeFloat() {
        return new float[]{getColor(0), getColor(1), getColor(2)};
    }

    /**
     * Sets the internal color representation of this color from the color atlas.
     *
     * @param color Color data.
     *
     * @apiNote This method is mostly for <strong>INTERNAL</strong> usage.
     */
    void setColorFromAtlas(int[] color);
}