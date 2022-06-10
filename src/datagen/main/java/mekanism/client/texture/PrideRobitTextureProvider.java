package mekanism.client.texture;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import mekanism.common.entity.RobitPrideSkinData;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PrideRobitTextureProvider implements DataProvider {
    private final DataGenerator generator;
    private final ExistingFileHelper helper;

    private static final String ROBIT_SKIN_PATH = "textures/entity/robit";

    public PrideRobitTextureProvider(DataGenerator generator, ExistingFileHelper helper) {
        this.generator = generator;
        this.helper = helper;
    }
    @Override
    public void run(CachedOutput cache) throws IOException {
        BufferedImage image = getTexture();
        for (RobitPrideSkinData skinData : RobitPrideSkinData.values()) {
            transform(
                    () -> {
                        ColorModel cm = image.getColorModel();
                        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                        WritableRaster raster = image.copyData(null);
                        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
                    },
                    skinData,
                    (prideTexture, index) -> {
                        String fileName = skinData.lowerCaseName();
                        index++;
                        if (index != 1)
                            fileName += index;
                        fileName += ".png";
                        Path path = this.generator.getOutputFolder().resolve(Paths.get(PackType.CLIENT_RESOURCES.getDirectory(), "mekanism", ROBIT_SKIN_PATH, fileName));

                        try (ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                             HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream)) {
                            ImageIO.write(prideTexture,"PNG", hashingoutputstream);
                            cache.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());

                        } catch (IOException e) {
                            throw new RuntimeException("couldn't write image", e);
                        }
                    }
            );
        }
    }

    /**
     * this uses a method annotated with {@link com.google.common.annotations.VisibleForTesting}
     */
    private BufferedImage getTexture() throws IOException {
        try {
            Resource resource = helper.getResource(new ResourceLocation("mekanism", ROBIT_SKIN_PATH + "/robit.png"), PackType.CLIENT_RESOURCES);
            return ImageIO.read(resource.open());
        } catch (IOException e) {
            throw new IOException("couldn't load robit texture", e);
        }
    }

    private void transform(Supplier<BufferedImage> basicRobitTextureFactory, RobitPrideSkinData data, BiConsumer<BufferedImage, Integer> saveImageCallback) {
        //generate all textures
        for (int rotationIndex = 0; rotationIndex < data.getColor().length; rotationIndex++) {
            BufferedImage image = basicRobitTextureFactory.get();
            for (int chainIndex = 0; chainIndex < 4; chainIndex++) {
                int maxStripeIndex = 9;
                if (chainIndex == 0 || chainIndex == 2)
                    maxStripeIndex = 3;
                for (int stripeIndex = 0; stripeIndex < maxStripeIndex; stripeIndex++) {
                    int x = 0;
                    int y = 0;
                    switch (chainIndex) {
                        case 0:
                            x = 2 - stripeIndex;
                            y = 2;
                            break;
                        case 1:
                            x = stripeIndex;
                            break;
                        case 2:
                            x = stripeIndex + 6;
                            y = 2;
                            break;
                        case 3:
                            x = 8 - stripeIndex;
                            y = 4;
                    }
                    x+=15;
                    paintStripe(image, rgb(stripeIndex, chainIndex, rotationIndex, data), x, y);
                }
            }
            saveImageCallback.accept(image, rotationIndex);
        }
    }

    /**
     *
     * @param stripeIndex stripeIndex on chain
     * @param chainIndex the chain index (left, top, right, bottom). It starts on the left to hide a potential seam on the bottom
     * @param rotationIndex
     * @param data
     * @return color at that position
     */
    private int rgb(int stripeIndex, int chainIndex, int rotationIndex, RobitPrideSkinData data) {
        int index = stripeIndex + rotationIndex;
        if (chainIndex > 2)
            index+=3;
        if (chainIndex > 1)
            index+=9;
        if (chainIndex > 0)
            index+=3;
        return data.getColor()[index%data.getColor().length];
    }

    private void paintStripe(BufferedImage robit, int colorRGB, int x, int y) {
        robit.setRGB(x,y, colorRGB);
        robit.setRGB(x,y+1, colorRGB);
    }

    @Override
    public String getName() {
        return "Texture Provider";
    }
}
