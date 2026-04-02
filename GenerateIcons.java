import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateIcons {
    public static void main(String[] args) throws Exception {
        // Normal Cell
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int black = 0xFF111111;
        int grey = 0xFF555555;
        int lightGrey = 0xFF999999;
        int cyanLight = 0xFF00FFFF;
        int cyanDark = 0xFF008888;
        
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int color = 0x00000000;
                
                // Outline
                if ((y == 2 && x >= 5 && x <= 10) || (y == 13 && x >= 5 && x <= 10) ||
                    (x == 4 && y >= 3 && y <= 12) || (x == 11 && y >= 3 && y <= 12)) color = black;
                // Caps
                else if ((y == 3 || y == 12) && x >= 5 && x <= 10) color = grey;
                // Body
                else if (x >= 5 && x <= 10 && y >= 4 && y <= 11) color = lightGrey;
                // Energy Core
                if (x >= 6 && x <= 9 && y >= 5 && y <= 10) color = cyanDark;
                if (x >= 7 && x <= 8 && y >= 6 && y <= 9) color = cyanLight;
                if ((x == 7 || x == 8) && (y == 7 || y == 8)) color = 0xFFFFFFFF; // White center
                
                img.setRGB(x, y, color);
            }
        }
        ImageIO.write(img, "png", new File("src/main/resources/assets/wintercore/textures/item/winter_energy_cell.png"));

        // Infinite Cell
        BufferedImage inf = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int magentaLight = 0xFFFF00FF;
        int magentaDark = 0xFF880088;
        
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int c = img.getRGB(x, y);
                if (c == cyanLight) c = magentaLight;
                else if (c == cyanDark) c = magentaDark;
                inf.setRGB(x, y, c);
            }
        }
        ImageIO.write(inf, "png", new File("src/main/resources/assets/wintercore/textures/item/winter_infinite_energy_cell.png"));
    }
}
