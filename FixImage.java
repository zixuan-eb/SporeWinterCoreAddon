import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Color;

public class FixImage {
    public static void main(String[] args) throws Exception {
        File c1 = new File("C:\\Users\\Administrator\\.gemini\\antigravity\\brain\\e752d40a-69e7-4165-906d-4c5b8ed6c0a0\\winter_energy_cell_1775104958197.png");
        BufferedImage img = ImageIO.read(c1);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                Color c = new Color(p, true);
                if (c.getRed() > 230 && c.getGreen() > 230 && c.getBlue() > 230) {
                    out.setRGB(x, y, 0x00000000);
                } else {
                    out.setRGB(x, y, p);
                }
            }
        }
        ImageIO.write(out, "png", new File("src/main/resources/assets/wintercore/textures/item/winter_energy_cell.png"));
    }
}
