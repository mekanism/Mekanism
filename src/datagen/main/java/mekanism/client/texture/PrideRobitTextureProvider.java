package mekanism.client.texture;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import mekanism.common.Mekanism;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataGenerator.PathProvider;
import net.minecraft.data.DataGenerator.Target;
import net.minecraft.data.DataProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PrideRobitTextureProvider implements DataProvider {

    private final DataGenerator generator;
    private final ExistingFileHelper helper;

    private static final String ROBIT_SKIN_PATH = "textures/entity/robit";

    public PrideRobitTextureProvider(DataGenerator generator, ExistingFileHelper helper) {
        this.generator = generator;
        this.helper = helper;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void run(@Nonnull CachedOutput cache) throws IOException {
        PathProvider pathProvider = generator.createPathProvider(Target.RESOURCE_PACK, ROBIT_SKIN_PATH);
        Resource resource = helper.getResource(MekanismRobitSkins.BASE.getRegistryName(), PackType.CLIENT_RESOURCES, ".png", ROBIT_SKIN_PATH);
        try (InputStream inputStream = resource.open()) {
            BufferedImage image = ImageIO.read(inputStream);
            for (RobitPrideSkinData skinData : RobitPrideSkinData.values()) {
                transform(() -> {
                          ColorModel cm = image.getColorModel();
                          boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                          WritableRaster raster = image.copyData(null);
                          return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
                      }, skinData, LamdbaExceptionUtils.rethrowBiConsumer((prideTexture, index) -> {
                          String fileName = skinData.lowerCaseName();
                          if (++index != 1) {
                              fileName += index;
                          }
                          Path path = pathProvider.file(Mekanism.rl(fileName), "png");
                          try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                               HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), outputStream)) {
                              ImageIO.write(prideTexture, "png", hashingOutputStream);
                              cache.writeIfNeeded(path, outputStream.toByteArray(), hashingOutputStream.hash());
                          }
                      })
                );
            }
        }
    }

    private void transform(Supplier<BufferedImage> basicRobitTextureFactory, RobitPrideSkinData data, BiConsumer<BufferedImage, Integer> saveImageCallback) {
        //generate all textures
        for (int rotationIndex = 0; rotationIndex < data.getColor().length; rotationIndex++) {
            BufferedImage image = basicRobitTextureFactory.get();
            for (int chainIndex = 0; chainIndex < 4; chainIndex++) {
                int maxStripeIndex = 9;
                if (chainIndex == 1 || chainIndex == 3) {
                    maxStripeIndex = 3;
                }
                for (int stripeIndex = 0; stripeIndex < maxStripeIndex; stripeIndex++) {
                    int x = 0;
                    int y = 0;
                    switch (chainIndex) {
                        case 0 -> {
                            x = 8 - stripeIndex;
                            y = 4;
                        }
                        case 1 -> {
                            x = 2 - stripeIndex;
                            y = 2;
                        }
                        case 2 -> x = stripeIndex;
                        case 3 -> {
                            x = stripeIndex + 6;
                            y = 2;
                        }
                    }
                    x += 15;
                    paintStripe(image, rgb(stripeIndex, chainIndex, rotationIndex, data), x, y);
                }
            }
            saveImageCallback.accept(image, rotationIndex);
        }
    }

    /**
     * @param stripeIndex Stripe Index on the chain.
     * @param chainIndex  The chain index (bottom, front(left), top, back(right)). It starts on the bottom to hide a potential seam on the bottom/back connection
     *
     * @return Color at that position
     */
    private int rgb(int stripeIndex, int chainIndex, int rotationIndex, RobitPrideSkinData data) {
        //offset it by 12, so the pride flag always starts at the top by default
        int index = stripeIndex + rotationIndex + 12;
        if (chainIndex > 2) {
            index += 9;
        }
        if (chainIndex > 1) {
            index += 3;
        }
        if (chainIndex > 0) {
            index += 9;
        }
        return data.getColor()[index % data.getColor().length];
    }

    private void paintStripe(BufferedImage robit, int colorRGB, int x, int y) {
        robit.setRGB(x, y, colorRGB);
        robit.setRGB(x, y + 1, colorRGB);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Texture Provider";
    }
}