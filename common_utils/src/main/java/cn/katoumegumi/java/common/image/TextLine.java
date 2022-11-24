package cn.katoumegumi.java.common.image;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本行
 */
public class TextLine {

    /**
     * 左上角x坐标
     */
    private int coordinateX;

    /**
     * 左上角y坐标
     */
    private int coordinateY;

    private final FontMetrics fontMetrics;

    /**
     * 字间距
     */
    private int wordSpace;

    /**
     * 文本长度
     */
    private int textWidth;

    private Color color;

    /**
     * 行包含的字符
     */
    private List<TextChar> characterList = new ArrayList<>();


    public TextLine(FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
    }

    public List<TextChar> getCharacterList() {
        return characterList;
    }

    public void setCharacterList(List<TextChar> characterList) {
        this.characterList = characterList;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public int getWordSpace() {
        return wordSpace;
    }

    public void setWordSpace(int wordSpace) {
        this.wordSpace = wordSpace;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public Color getColor() {
        return color;
    }

    public TextLine setColor(Color color) {
        this.color = color;
        return this;
    }
}
