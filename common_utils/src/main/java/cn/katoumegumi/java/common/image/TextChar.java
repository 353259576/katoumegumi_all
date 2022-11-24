package cn.katoumegumi.java.common.image;

import java.awt.*;

/**
 * 文本字符
 */
public class TextChar {

    /**
     * 左上角x坐标
     */
    private int coordinateX;

    /**
     * 左上角y坐标
     */
    private int coordinateY;

    private final FontMetrics fontMetrics;

    private final Character character;

    private int width;

    private Color color;

    public TextChar(FontMetrics fontMetrics, Character character) {
        this.fontMetrics = fontMetrics;
        this.character = character;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public TextChar setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
        return this;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public TextChar setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
        return this;
    }

    public Character getCharacter() {
        return character;
    }

    public int getWidth() {
        return width;
    }

    public TextChar setWidth(int width) {
        this.width = width;
        return this;
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public Color getColor() {
        return color;
    }

    public TextChar setColor(Color color) {
        this.color = color;
        return this;
    }
}
