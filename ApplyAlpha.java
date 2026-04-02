import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ApplyAlpha {
    public static void main(String[] args) throws Exception {
        String[] files = {"magic_beam.png", "magic_circle.png", "pulse_wave.png", "winter_star.png"};
        for (String filename : files) {
            File f = new File("src/main/resources/assets/wintercore/textures/effect/" + filename);
            if (!f.exists()) continue;
            BufferedImage img = ImageIO.read(f);
            BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int p = img.getRGB(x, y);
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;
                    int brightness = (r + g + b) / 3;
                    // Map brightness to alpha. Pure black = transparent
                    if (brightness < 10) {
                        out.setRGB(x, y, 0x00000000); // Fully transparent
                    } else {
                        out.setRGB(x, y, (brightness << 24) | (r << 16) | (g << 8) | b);
                    }
                }
            }
            ImageIO.write(out, "png", f);
        }
    }
}
