import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class UnpremultiplyAlpha {
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
                    // The previous script mapped brightness to alpha and kept the original rgb.
                    // This means rgb was pre-multiplied!
                    // Let's recover the original (pre-multiplied) rgb by treating the current RGB as pre-multiplied.
                    // Actually, the current image has alpha == brightness and rgb == orig_rgb.
                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;
                    // Because I already processed it in the last step, a == brightness, and r,g,b are the original.
                    // If a == 0, transparent
                    if (a < 5) {
                        out.setRGB(x, y, 0x00000000);
                    } else {
                        // Unpremultiply
                        float r_f = (float)r / a;
                        float g_f = (float)g / a;
                        float b_f = (float)b / a;
                        // Avoid blown out highlights, clamp
                        int r_new = Math.min(255, (int)(r_f * 255));
                        int g_new = Math.min(255, (int)(g_f * 255));
                        int b_new = Math.min(255, (int)(b_f * 255));
                        
                        // We also can scale Alpha so it's not totally opaque.
                        // Let's keep alpha as is.
                        out.setRGB(x, y, (a << 24) | (r_new << 16) | (g_new << 8) | b_new);
                    }
                }
            }
            ImageIO.write(out, "png", f);
        }
    }
}
