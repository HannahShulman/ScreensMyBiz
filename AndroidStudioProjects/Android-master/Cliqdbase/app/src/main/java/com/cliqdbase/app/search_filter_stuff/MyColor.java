package com.cliqdbase.app.search_filter_stuff;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuval on 05/08/2015.
 *
 * @author Yuval Siev
 */
public class MyColor {

    public static final int BLACK = 0xff000000;
    public static final int BLUE = 0xff0000FF;
    public static final int BROWN = 0xff8B4513;
    public static final int CYAN = 0xff00ffff;
    public static final int DARK_GRAY = 0xff444444;
    public static final int HAZEL = 0xff594c26;
    public static final int RED = 0xFFFF0000;
    public static final int GRAY = 0xff888888;
    public static final int GREEN = 0xFF00FF00;
    public static final int DARK_GREEN = 0xFF006400;
    public static final int YELLOW = 0xffffff00;
    public static final int ORANGE = 0xffffa500;
    public static final int LIGHT_PINK = 0xffffb6c1;
    public static final int PINK = 0xffff69b4;
    public static final int DARK_PINK = 0xffff1493;
    public static final int PURPLE = 0xff551A8B;
    public static final int VIOLET = 0xff9f00ff;
    public static final int WHITE = 0xffffffff;


    public static final int SKIN_DARK = 0xFF5B0001;
    public static final int SKIN_BROWN = 0xFFBA6C49;
    public static final int SKIN_LIGHT = 0xFFFFDFC4;

    public static final int BLONDE = 0xFFC0AA73;
    public static final int BRUNETTE = 0xFF906245;
    public static final int DARK_BROWN = 0xFF332923;
    public static final int REDHEAD = 0xFF6F200A;
    public static final int GINGER = 0xFFAF390B;

    private static final Integer OTHER = null;

    private Integer exactColorCode;
    private String prettyString;

    private MyColor(Integer exactColorCode, String prettyString) {
        this.exactColorCode = exactColorCode;
        this.prettyString = prettyString;
    }


    public static MyColor getInstance(Integer colorCode) {
        if (colorCode == null)
            return new MyColor(null, "Other");

        String prettyString;
        switch (colorCode) {
            case BLACK:
                prettyString = "Black";
                break;
            case BLUE:
                prettyString = "Blue";
                break;
            case BROWN:
                prettyString = "Brown";
                break;
            case CYAN:
                prettyString = "Cyan";
                break;
            case DARK_GRAY:
                prettyString = "Dark Gray";
                break;
            case HAZEL:
                prettyString = "Hazel";
                break;
            case RED:
                prettyString = "Red";
                break;
            case GRAY:
                prettyString = "Gray";
                break;
            case GREEN:
                prettyString = "Green";
                break;
            case SKIN_LIGHT:
                prettyString = "Light Skin";
                break;
            case SKIN_BROWN:
                prettyString = "Brown Skin";
                break;
            case SKIN_DARK:
                prettyString = "Dark Skin";
                break;
            case BLONDE:
                prettyString = "Blonde";
                break;
            case BRUNETTE:
                prettyString = "Brunette";
                break;
            case DARK_BROWN:
                prettyString = "Dark Brown";
                break;
            case REDHEAD:
                prettyString = "Redhead";
                break;
            case GINGER:
                prettyString = "Ginger";
                break;
            case DARK_GREEN:
                prettyString = "Dark Green";
                break;
            case YELLOW:
                prettyString = "Yellow";
                break;
            case ORANGE:
                prettyString = "Orange";
                break;
            case LIGHT_PINK:
                prettyString = "Light Pink";
                break;
            case PINK:
                prettyString = "Pink";
                break;
            case DARK_PINK:
                prettyString = "Dark Pink";
                break;
            case PURPLE:
                prettyString = "Purple";
                break;
            case VIOLET:
                prettyString = "Violet";
                break;
            case WHITE:
                prettyString = "White";
                break;

            default:
                prettyString = null;
        }
        return new MyColor(colorCode, prettyString);
    }

    public String getPrettyString() {
        if (this.prettyString != null)
            return this.prettyString;

        return getExactColorCodeString();
    }

    public String getExactColorCodeString() {
        return "#" + Integer.toHexString(this.exactColorCode);
    }

    public int getExactColorCode() {
        return this.exactColorCode;
    }


    public static List<MyColor> getEyeColorList() {
        List<MyColor> list = new ArrayList<>();

        list.add(getInstance(BLACK));
        list.add(getInstance(BLUE));
        list.add(getInstance(BROWN));
        list.add(getInstance(CYAN));
        list.add(getInstance(DARK_GRAY));
        list.add(getInstance(HAZEL));
        list.add(getInstance(RED));
        list.add(getInstance(GRAY));
        list.add(getInstance(GREEN));
        list.add(getInstance(OTHER));

        return list;
    }

    public static List<MyColor> getHairColorList() {
        List<MyColor> list = new ArrayList<>();

        list.add(getInstance(BLONDE));
        list.add(getInstance(BRUNETTE));
        list.add(getInstance(REDHEAD));
        list.add(getInstance(GINGER));
        list.add(getInstance(BROWN));
        list.add(getInstance(DARK_BROWN));
        list.add(getInstance(BLACK));
        list.add(getInstance(OTHER));

        return list;
    }

    public static List<MyColor> getSkinColorList() {
        List<MyColor> list = new ArrayList<>();

        list.add(getInstance(SKIN_LIGHT));
        list.add(getInstance(SKIN_BROWN));
        list.add(getInstance(SKIN_DARK));
        list.add(getInstance(OTHER));

        return list;
    }

    public static List<MyColor> getClothesColorList() {
        List<MyColor> list = new ArrayList<>();

        list.add(getInstance(BLACK));
        list.add(getInstance(BLUE));
        list.add(getInstance(BROWN));
        list.add(getInstance(CYAN));
        list.add(getInstance(DARK_GRAY));
        list.add(getInstance(RED));
        list.add(getInstance(GRAY));
        list.add(getInstance(GREEN));
        list.add(getInstance(DARK_GREEN));
        list.add(getInstance(YELLOW));
        list.add(getInstance(ORANGE));
        list.add(getInstance(LIGHT_PINK));
        list.add(getInstance(PINK));
        list.add(getInstance(DARK_PINK));
        list.add(getInstance(PURPLE));
        list.add(getInstance(VIOLET));
        list.add(getInstance(WHITE));
        list.add(getInstance(OTHER));

        return list;
    }


    public boolean isOther() {
        return (this.exactColorCode == null);
    }

    /**
     * Returns the MyColor object from the given string. If unsuccessful, returns null.
     *
     * String options: #AARRGGBB, #RRGGBB,
     *
     //* @param color    The color string to parse.
     * @return  The MyColor parsed from the given string, or null if there was an error during parsing.
     */
    /*public static MyColor parseColor(String color) {
        if (color == null || color.trim().isEmpty())
            return null;
        color = color.trim();
        if (color.charAt(0) == '#') {
            try {
                int exactColor = Color.parseColor(color);
                return getInstance(exactColor);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        else {      // The color is a 'pretty string'
            MyColor myColor;
            switch(color.toLowerCase()) {
                case "black":
                    myColor = getInstance(BLACK);
                    break;
                case "blue":
                    myColor = getInstance(BLUE);
                    break;
                case "brown":
                    myColor = getInstance(BROWN);
                    break;
                case "cyan":
                    myColor = getInstance(CYAN);
                    break;
                case "dark gray":
                    myColor = getInstance(DARK_GRAY);
                    break;
                case "hazel":
                    myColor = getInstance(HAZEL);
                    break;
                case "red":
                    myColor = getInstance(RED);
                    break;
                case "gray":
                    myColor = getInstance(GRAY);
                    break;
                case "green":
                    myColor = getInstance(GREEN);
                    break;
                case "light skin":
                    myColor = getInstance(SKIN_LIGHT);
                    break;
                case "brown skin":
                    myColor = getInstance(SKIN_BROWN);
                    break;
                case "dark skin":
                    myColor = getInstance(SKIN_DARK);
                    break;
                case "blonde":
                    myColor = getInstance(BLONDE);
                    break;
                case "brunette":
                    myColor = getInstance(BRUNETTE);
                    break;
                case "dark brown":
                    myColor = getInstance(DARK_BROWN);
                    break;
                case "redhead":
                    myColor = getInstance(REDHEAD);
                    break;
                case "ginger":
                    myColor = getInstance(GINGER);
                    break;
                case "dark green":
                    myColor = getInstance(DARK_GREEN);
                    break;
                case "yellow":
                    myColor = getInstance(YELLOW);
                    break;
                case "orange":
                    myColor = getInstance(ORANGE);
                    break;
                case "light pink":
                    myColor = getInstance(LIGHT_PINK);
                    break;
                case "pink":
                    myColor = getInstance(PINK);
                    break;
                case "dark pink":
                    myColor = getInstance(DARK_PINK);
                    break;
                case "purple":
                    myColor = getInstance(PURPLE);
                    break;
                case "violet":
                    myColor = getInstance(VIOLET);
                    break;
                case "white":
                    myColor = getInstance(WHITE);
                    break;


                default:
                    return null;
            }
            return myColor;
        }
    }*/


    public static Integer findClosest(int exactColorCode, List<MyColor> colors) {
        double minMse = 0xFE01;     // The maximum value of the MSE. This is (0xFF)^2.
        Integer listColorWithLowestMSE = null;
        for (MyColor color : colors) {
            if (color.isOther())
                continue;
            int colorValue = color.getExactColorCode();
            double mse = colorMSE(exactColorCode, colorValue);
            if (mse <= minMse) {
                minMse = mse;
                listColorWithLowestMSE = colorValue;
            }
        }
        return listColorWithLowestMSE;
    }

    /**
     * Calculating the Mean Squared Error (MSE) between two colors, based on their alpha, red, green, blue parts.
     * @param color1    The first color.
     * @param color2    The second color.
     * @return The MSE value.
     */
    private static double colorMSE(int color1, int color2) {
        int color1Alpha = Color.alpha(color1);
        int color1Red = Color.red(color1);
        int color1Green = Color.green(color1);
        int color1Blue = Color.blue(color1);

        int color2Alpha = Color.alpha(color2);
        int color2Red = Color.red(color2);
        int color2Green = Color.green(color2);
        int color2Blue = Color.blue(color2);

        return (Math.pow(color1Alpha - color2Alpha, 2) + Math.pow(color1Red - color2Red, 2) + Math.pow(color1Green - color2Green, 2) + Math.pow(color1Blue - color2Blue, 2))/4.0;
    }

}
