package buildcraft.api.tablet;

public class TabletBitmap {
    public final int width, height;
    protected int[] data;

    public TabletBitmap(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new int[width * height];
    }

    public TabletBitmap(ITablet tablet) {
        this(tablet.getScreenWidth(), tablet.getScreenHeight());
    }

    public int[] getData() {
        return data;
    }

    public int get(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return 0;
        }
        return data[y * width + x];
    }

    public void set(int x, int y, int shade) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        data[y * width + x] = shade;
    }

    public void set(int x, int y, TabletBitmap bitmap) {
        for (int i = 0; i < bitmap.height; i++) {
            if (i >= height) {
                break;
            }
            for (int h = 0; h < bitmap.width; h++) {
                if (h >= width) {
                    break;
                }

                set(x + h, y + i, bitmap.get(h, i));
            }
        }
    }

    public TabletBitmap duplicate() {
        TabletBitmap cloned = new TabletBitmap(this.width, this.height);
        cloned.data = this.data.clone();
        return cloned;
    }
}
