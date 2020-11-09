package me.earth.phobos.manager;

import me.earth.phobos.util.ColorUtil;

import java.awt.*;

public class ColorManager {

    private float red;
    private float green;
    private float blue;
    private float alpha;
    private Color color;

    public ColorManager() {
        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;
        this.alpha = 1.0f;
        this.color = new Color(this.red, this.green, this.blue, this.alpha);
    }

    public Color getColor() {
        return this.color;
    }

    public int getColorAsInt() {
        return ColorUtil.toRGBA(this.color);
    }

    public int getColorAsIntFullAlpha() {
        return ColorUtil.toRGBA(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 255));
    }

    public int getColorWithAlpha(int alpha) {
        return ColorUtil.toRGBA(new Color(this.red, this.green, this.blue, alpha / 255.0f));
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.updateColor();
    }

    public void updateColor() {
        this.setColor(new Color(this.red, this.green, this.blue, this.alpha));
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(int red, int green, int blue, int alpha) {
        this.red = red / 255.0f;
        this.green = green / 255.0f;
        this.blue = blue / 255.0f;
        this.alpha = alpha / 255.0f;
        this.updateColor();
    }

    public void setRed(float red) {
        this.red = red;
        this.updateColor();
    }

    public void setGreen(float green) {
        this.green = green;
        this.updateColor();
    }

    public void setBlue(float blue) {
        this.blue = blue;
        this.updateColor();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.updateColor();
    }
}
