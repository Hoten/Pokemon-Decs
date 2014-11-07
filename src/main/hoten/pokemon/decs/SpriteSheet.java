package hoten.pokemon.decs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteSheet {

    private final BufferedImage _spriteSheet;
    private final int _numSpritesInRow;
    private final int _spriteSize;

    public SpriteSheet(String fileName, int numSpritesInRow) throws IOException {
        _numSpritesInRow = numSpritesInRow;
        _spriteSheet = ImageIO.read(new File(fileName));
        _spriteSize = _spriteSheet.getWidth() / numSpritesInRow;
    }

    public BufferedImage getSprite(int index) {
        int x = index % _numSpritesInRow;
        int y = index / _numSpritesInRow;
        return _spriteSheet.getSubimage(x * _spriteSize, y * _spriteSize, _spriteSize, _spriteSize);
    }
}
