import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TintImage {
    public static void main(String[] args) throws Exception {
        File c1 = new File("src/main/resources/assets/wintercore/textures/item/winter_energy_cell.png");
        BufferedImage img = ImageIO.read(c1);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                
                // If it's cyan-ish (g and b are high, r is low)
                if (g > 100 && b > 100 && r < 150) {
                    // Change to Magenta (r and b are high, g is low)
                    r = b;
                    g = r / 3;
                }
                
                p = (a << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, p);
            }
        }
        ImageIO.write(out, "png", new File("src/main/resources/assets/wintercore/textures/item/winter_infinite_energy_cell.png"));
    }
}
