package cn.katoumegumi.java.common.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * 文本域
 */
public class WsTextArea {

    private static final int TEXT_ALIGN_MASK = 255;

    /**
     * 左上角x坐标
     */
    private int coordinateX;

    /**
     * 左上角y坐标
     */
    private int coordinateY;


    private int width;

    private int height;


    private final FontMetrics fontMetrics;

    /**
     * 文本内容
     */
    private final String text;

    /**
     * 字间距
     */
    private int wordSpace;

    /**
     * 行间距
     */
    private int lineSpace;

    /**
     * 对齐方式
     * 水平(1-8)：1 左对齐 2 居中 3 右对齐 4 分散对齐
     * 垂直(9-16) 1 上对齐 2 居中 3 下对齐 4 分散对齐
     */
    private int textAlign;

    /**
     * 边距
     */
    private WsMargin margin;

    private Color color;



    private List<TextLine> textLineList = new ArrayList<>();


    public WsTextArea(FontMetrics fontMetrics, String text) {
        this.fontMetrics = fontMetrics;
        this.text = text;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public String getText() {
        return text;
    }

    public int getWordSpace() {
        return wordSpace;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public int getTextAlign() {
        return textAlign;
    }

    public List<TextLine> getTextLineList() {
        return textLineList;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public WsTextArea setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
        return this;
    }

    public WsTextArea setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
        return this;
    }

    public WsTextArea setWidth(int width) {
        this.width = width;
        return this;
    }

    public WsTextArea setHeight(int height) {
        this.height = height;
        return this;
    }

    public WsTextArea setWordSpace(int wordSpace) {
        this.wordSpace = wordSpace;
        return this;
    }

    public WsTextArea setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
        return this;
    }

    public WsTextArea setTextAlign(int textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public WsTextArea setColor(Color color) {
        this.color = color;
        return this;
    }

    public WsTextArea setTextLineList(List<TextLine> textLineList) {
        this.textLineList = textLineList;
        return this;
    }

    public WsMargin getMargin() {
        return margin;
    }

    public WsTextArea setMargin(WsMargin margin) {
        this.margin = margin;
        return this;
    }

    /**
     * 获取各个字符宽度
     * @param chars
     * @return
     */
    private int[] getCharWidths(char[] chars){
        int[] charWidths = new int[chars.length];
        Map<Character,Integer> charWidthCacheMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            if(chars[i] == '\n'){
                continue;
            }
            Integer width = charWidthCacheMap.get(chars[i]);
            if(width == null){
                width = getFontMetrics().charWidth(chars[i]);
                charWidthCacheMap.put(chars[i],width);
            }
            charWidths[i] = width;

        }
        return charWidths;
    }

    public WsTextArea build(){
        textLineList.clear();
        char[] chars = getText().toCharArray();
        int[] charWidths = getCharWidths(chars);

        int horizontalType = getTextAlign() & TEXT_ALIGN_MASK;
        int verticalType = (getTextAlign() >> 8) & TEXT_ALIGN_MASK;
        int fontSize = getFontMetrics().getFont().getSize();
        if(horizontalType == 4){
            this.wordSpace = 0;
        }
        if(verticalType == 4){
            this.lineSpace = 0;
        }

        int currentCoordinateX = getCoordinateX();
        int currentCoordinateY = getCoordinateY();
        int maxWidth = getWidth();
        int maxHeight = getHeight();
        WsMargin wsMargin = getMargin();
        if(wsMargin != null){
            maxWidth -= wsMargin.getLeft() + wsMargin.getRight();
            maxHeight -= wsMargin.getTop() + wsMargin.getBottom();
            currentCoordinateX += wsMargin.getLeft();
            currentCoordinateY += wsMargin.getTop();
        }
        int currentLineSpase = getLineSpace();
        int currentWordSpase = getWordSpace();

        List<TextLine> lineList = getTextLineList();
        TextLine currentTextLine = new TextLine(getFontMetrics());
        int currentLineWidth = 0;
        int currentLineHeight = 0;
        boolean needWrap = false;
        for (int i = 0; i < chars.length; i++){
            if(chars[i] == '\n'){
                //换行
                needWrap = true;
            }else {
                if(currentTextLine.getCharacterList().isEmpty()){
                    //每行第一个字
                    currentLineWidth += charWidths[i];
                }else {
                    currentLineWidth += charWidths[i] + currentWordSpase;
                }
                if(currentLineWidth > maxWidth){
                    //超过最大限制
                    if(currentTextLine.getCharacterList().isEmpty()){
                        currentLineWidth -= charWidths[i];
                    }else {
                        currentLineWidth -= charWidths[i] + currentWordSpase;
                    }
                    needWrap = true;
                    i--;
                }else {
                    currentTextLine.getCharacterList().add(new TextChar(getFontMetrics(),chars[i]).setWidth(charWidths[i]).setColor(getColor()));
                }
            }
            if(needWrap){
                currentTextLine.setTextWidth(currentLineWidth);
                lineList.add(currentTextLine);
                currentTextLine = new TextLine(getFontMetrics()).setColor(getColor());
                currentLineWidth = 0;
                currentLineHeight += fontSize + currentLineSpase;
                if(currentLineHeight + fontSize + currentLineSpase > maxHeight){
                    //检查是否容下下一行
                    break;
                }
                needWrap = false;
            }
        }
        if(!currentTextLine.getCharacterList().isEmpty() && currentLineHeight + fontSize + currentLineSpase <= maxHeight){
            //检查是否存在最后一行
            currentTextLine.setTextWidth(currentLineWidth);
            lineList.add(currentTextLine);
            currentLineHeight += fontSize;
        }

        int startY = currentCoordinateY;
        switch (verticalType){
            case 1:break;
            case 2:
                startY += (maxHeight - currentLineHeight)/2;
                //居中
                break;
            case 3://下对齐
                startY += (maxHeight - currentLineHeight);
                break;
            case 4:
                //两端对齐
                if(lineList.size() > 1){
                    currentLineSpase = (maxHeight - currentLineHeight)/(lineList.size() - 1);
                    setLineSpace(currentLineSpase);
                }else {
                    startY += (maxHeight - currentLineHeight)/2;
                }
                break;
            default:throw new IllegalArgumentException("不支持的");
        }

        for (TextLine textLine : lineList) {
            textLine.setCoordinateY(startY);
            int startX = currentCoordinateX;
            switch (horizontalType) {
                case 1:
                    textLine.setWordSpace(currentWordSpase);
                    break;
                case 2:
                    startX += (maxWidth - textLine.getTextWidth()) / 2;
                    textLine.setWordSpace(currentWordSpase);
                    break;
                case 3:
                    startX += (maxWidth - textLine.getTextWidth());
                    textLine.setWordSpace(currentWordSpase);
                    break;
                case 4:
                    if (textLine.getCharacterList().size() > 1) {
                        currentWordSpase = (maxWidth - textLine.getTextWidth()) / (textLine.getCharacterList().size() - 1);
                        textLine.setWordSpace(currentWordSpase);
                    } else if (textLine.getCharacterList().size() == 1) {
                        startX = (maxWidth - textLine.getTextWidth()) / 2;
                    }
                    break;
            }
            for (TextChar textChar : textLine.getCharacterList()) {
                textChar.setCoordinateX(startX)
                        .setCoordinateY(startY);
                startX += textChar.getWidth() + textLine.getWordSpace();
            }
            startY += fontSize + currentLineSpase;
        }
        return this;
    }

    public WsTextArea draw(BufferedImage bufferedImage){
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return draw(graphics2D);
    }

    public WsTextArea draw(Graphics graphics){
        Font oldFont = graphics.getFont();
        Color oldColor = graphics.getColor();
        FontMetrics currentFontMetrics = getFontMetrics();
        Font currentFont = currentFontMetrics.getFont();
        Color currentColor = getColor();
        boolean isChangeFont = false;
        boolean isChangeColor = false;
        if(currentColor != null){
            if(!currentColor.equals(oldColor)){
                graphics.setColor(currentColor);
                isChangeColor = true;
            }
        }
        if(!oldFont.equals(currentFont)){
            graphics.setFont(currentFont);
            isChangeFont = true;
        }
        graphics.setFont(currentFontMetrics.getFont());
        int difference = currentFontMetrics.getHeight() - currentFontMetrics.getFont().getSize();
        if(difference > 0){
            difference /= 2;
        }
        for (TextLine textLine : getTextLineList()) {
            for (TextChar textChar : textLine.getCharacterList()) {
                graphics.drawString(textChar.getCharacter().toString(), textChar.getCoordinateX(), textChar.getCoordinateY() + fontMetrics.getAscent() - difference);
            }
        }
        if(isChangeFont) {
            graphics.setFont(oldFont);
        }
        if(isChangeColor) {
            graphics.setColor(oldColor);
        }
        return this;
    }




}
