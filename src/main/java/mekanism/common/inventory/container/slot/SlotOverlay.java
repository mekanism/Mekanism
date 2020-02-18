package mekanism.common.inventory.container.slot;

//TODO: Keep this with ContainerSlotType. If we move that to the API should we make a variant of this that has no position info?
// and then leave the position information to the GUI
public enum SlotOverlay {
    MINUS(18, 18, 0, 18),
    PLUS(18, 18, 18, 18),
    POWER(18, 18, 36, 18),
    INPUT(18, 18, 54, 18),
    OUTPUT(18, 18, 72, 18),
    CHECK(18, 18, 0, 36);

    public final int width;
    public final int height;

    public final int textureX;
    public final int textureY;

    SlotOverlay(int w, int h, int x, int y) {
        width = w;
        height = h;

        textureX = x;
        textureY = y;
    }
}