package hoten.pokemon.decs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageBuilder {

    private BufferedImage _result;

    public ImageBuilder(BufferedImage initial) {
        BufferedImage copy = new BufferedImage(initial.getWidth(), initial.getHeight(), initial.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(initial, 0, 0, null);
        g.dispose();
        _result = copy;
    }

    public BufferedImage getResult() {
        return _result;
    }

    public ImageBuilder drawAt(BufferedImage backgroundImage, int x, int y) {
        _result.getGraphics().drawImage(backgroundImage, x, y, null);
        return this;
    }

    public ImageBuilder drawAtCenter(BufferedImage backgroundImage) {
        int x = (_result.getWidth() - backgroundImage.getWidth()) / 2;
        int y = (_result.getHeight() - backgroundImage.getHeight()) / 2;
        _result.getGraphics().drawImage(backgroundImage, x, y, null);
        return this;
    }

    public ImageBuilder drawTextCentered(String text, int y) {
        Graphics2D g = (Graphics2D) _result.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font font = new Font("Consolas", Font.PLAIN, 80);
        g.setFont(font);

        Rectangle2D stringRect = font.getStringBounds(text, g.getFontMetrics().getFontRenderContext());
        int middleX = (int) ((_result.getWidth() - stringRect.getWidth()) / 2);

        g.setStroke(new BasicStroke(4));

        TextLayout tl = new TextLayout(text, font, g.getFontRenderContext());

        Shape shape = tl.getOutline(null);
        g.translate(middleX, y);
        g.setColor(Color.BLACK);
        g.draw(shape);
        g.setColor(Color.WHITE);
        g.drawString(text, 0, 0);
        g.dispose();

        return this;
    }

    public ImageBuilder resize(double sf) {
        int newW = (int) (sf * _result.getWidth());
        int newH = (int) (sf * _result.getHeight());
        Image tmp = _result.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        _result = dimg;

        return this;
    }
}
