package seda_project.control_alt_defeat.gamebox.Memory.engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// this part is AI generated
public class SymbolLoader {

    private static final String SYMBOL_PATH = "src/main/resources/Images/Memory";
    private static final String CARD_BACK_PATH = SYMBOL_PATH + "/backface.png";
    private static final Map<Integer, BufferedImage> symbolCache = new HashMap<>();
    private static BufferedImage cardBackCache = null;

    public static BufferedImage getSymbolImage(int symbolId) {
        if (symbolCache.containsKey(symbolId)) {
            return symbolCache.get(symbolId);
        }

        String resourceName = SYMBOL_PATH + symbolId + ".png";
        BufferedImage img = loadImage(resourceName);
        if (img != null) {
            symbolCache.put(symbolId, img);
        }
        return img;
    }

    public static BufferedImage getCardBackImage() {
        if (cardBackCache == null) {
            cardBackCache = loadImage(CARD_BACK_PATH);
        }
        return cardBackCache;
    }

    private static BufferedImage loadImage(String path) {
        try (InputStream is = SymbolLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error loading image at " + path + ": " + e.getMessage());
            return null;
        }
    }
}