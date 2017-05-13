package com.cliqdbase.app.server_model;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author Yuval
 *
 */
public class SearchFilter {

    private static DualHashBidiMap<Integer, String> hairStyleCodes = new DualHashBidiMap<>();
    private static DualHashBidiMap<Integer, String> bodyTypeCodes = new DualHashBidiMap<>();
    private static DualHashBidiMap<Integer, String> clothingCodes = new DualHashBidiMap<>();

    private Integer hairStyleCode;
    private Integer bodyTypeCode;
    private byte[] clothingCode;
    private Integer clothingColorList;
    private Integer clothingColorAccurate;
    private Integer hairColorList;
    private Integer hairColorAccurate;
    private Integer eyesColorList;
    private Integer eyesColorAccurate;
    private Integer skinColorList;
    private Integer skinColorAccurate;
    private Integer heightInCm;
    private boolean unitsInMetric;		// if false, the device will have to convert the height in centimeters to feet and inches.


    public SearchFilter(Integer hairStyle, Integer bodyType,
                        byte[] clothing, Integer clothingColorList,
                        Integer clothingColorAccurate, Integer hairColorList,
                        Integer hairColorAccurate, Integer eyesColorList, Integer eyesColorAccurate,
                        Integer skinColorList, Integer skinColorAccurate, Integer heightInCm,
                        boolean unitsInMetric) {
        this.hairStyleCode = hairStyle;
        this.bodyTypeCode = bodyType;
        this.clothingCode = clothing;
        this.clothingColorList = clothingColorList;
        this.clothingColorAccurate = clothingColorAccurate;
        this.hairColorList = hairColorList;
        this.hairColorAccurate = hairColorAccurate;
        this.eyesColorList = eyesColorList;
        this.eyesColorAccurate = eyesColorAccurate;
        this.skinColorList = skinColorList;
        this.skinColorAccurate = skinColorAccurate;
        this.heightInCm = heightInCm;
        this.unitsInMetric = unitsInMetric;
    }

    public Integer getHairStyleCode() {
        return hairStyleCode;
    }


    public Integer getBodyTypeCode() {
        return bodyTypeCode;
    }


    public byte[] getClothingCode() {
        return clothingCode;
    }

    public Integer getClothingColorAccurate() {
        return clothingColorAccurate;
    }

    public Integer getHairColorAccurate() {
        return hairColorAccurate;
    }

    public Integer getEyesColorAccurate() {
        return eyesColorAccurate;
    }

    public Integer getSkinColorAccurate() {
        return skinColorAccurate;
    }

    public Integer getHeightInCm() {
        return heightInCm;
    }

    public boolean isUnitsInMetric() {
        return unitsInMetric;
    }


    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchFilter that = (SearchFilter) o;

        if (unitsInMetric != that.unitsInMetric) return false;
        if (hairStyleCode != null ? !hairStyleCode.equals(that.hairStyleCode) : that.hairStyleCode != null)
            return false;
        if (bodyTypeCode != null ? !bodyTypeCode.equals(that.bodyTypeCode) : that.bodyTypeCode != null)
            return false;
        if (!Arrays.equals(clothingCode, that.clothingCode)) return false;
        if (clothingColorList != null ? !clothingColorList.equals(that.clothingColorList) : that.clothingColorList != null)
            return false;
        if (clothingColorAccurate != null ? !clothingColorAccurate.equals(that.clothingColorAccurate) : that.clothingColorAccurate != null)
            return false;
        if (hairColorList != null ? !hairColorList.equals(that.hairColorList) : that.hairColorList != null)
            return false;
        if (hairColorAccurate != null ? !hairColorAccurate.equals(that.hairColorAccurate) : that.hairColorAccurate != null)
            return false;
        if (eyesColorList != null ? !eyesColorList.equals(that.eyesColorList) : that.eyesColorList != null)
            return false;
        if (eyesColorAccurate != null ? !eyesColorAccurate.equals(that.eyesColorAccurate) : that.eyesColorAccurate != null)
            return false;
        if (skinColorList != null ? !skinColorList.equals(that.skinColorList) : that.skinColorList != null)
            return false;
        if (skinColorAccurate != null ? !skinColorAccurate.equals(that.skinColorAccurate) : that.skinColorAccurate != null)
            return false;
        return !(heightInCm != null ? !heightInCm.equals(that.heightInCm) : that.heightInCm != null);

    }

    @Override
    public int hashCode() {
        int result = hairStyleCode != null ? hairStyleCode.hashCode() : 0;
        result = 31 * result + (bodyTypeCode != null ? bodyTypeCode.hashCode() : 0);
        result = 31 * result + (clothingCode != null ? Arrays.hashCode(clothingCode) : 0);
        result = 31 * result + (clothingColorList != null ? clothingColorList.hashCode() : 0);
        result = 31 * result + (clothingColorAccurate != null ? clothingColorAccurate.hashCode() : 0);
        result = 31 * result + (hairColorList != null ? hairColorList.hashCode() : 0);
        result = 31 * result + (hairColorAccurate != null ? hairColorAccurate.hashCode() : 0);
        result = 31 * result + (eyesColorList != null ? eyesColorList.hashCode() : 0);
        result = 31 * result + (eyesColorAccurate != null ? eyesColorAccurate.hashCode() : 0);
        result = 31 * result + (skinColorList != null ? skinColorList.hashCode() : 0);
        result = 31 * result + (skinColorAccurate != null ? skinColorAccurate.hashCode() : 0);
        result = 31 * result + (heightInCm != null ? heightInCm.hashCode() : 0);
        result = 31 * result + (unitsInMetric ? 1 : 0);
        return result;
    }

    public static void setHairStyleCodes(DualHashBidiMap<Integer, String> hairStyleCodes) {
        SearchFilter.hairStyleCodes = hairStyleCodes;
    }

    public static void setBodyTypeCodes(DualHashBidiMap<Integer, String> bodyTypeCodes) {
        SearchFilter.bodyTypeCodes = bodyTypeCodes;
    }

    public static void setClothingCodes(DualHashBidiMap<Integer, String> clothingCodes) {
        SearchFilter.clothingCodes = clothingCodes;
    }

    public static String getBodyTypeFromCode(int code) {
        return bodyTypeCodes.get(code);
    }

    public static String getHairStyleFromCode(int code) {
        return hairStyleCodes.get(code);
    }

    public static String getClothingFromCode(byte[] code) {
        if (code == null)
            return null;
        String clothesString = "";

        int bitCounter = 0;
        for (int i = code.length - 1; i>= 0; i--) {
            byte tempByte = 0b00000001;

            while (tempByte != 0) {
                bitCounter++;
                if ((tempByte & code[i]) != 0) {
                    String cloth = clothingCodes.get(bitCounter);
                    if (!clothesString.isEmpty())
                        cloth = ", " + cloth;

                    clothesString += cloth;
                }
                tempByte <<= 1;
            }
        }
        return clothesString;
    }

    public static byte[] getClothingCodeFromString(String clothing) {
        if (clothing == null || clothing.trim().isEmpty())
            return null;

        String[] clothes = clothing.split(",");

        ArrayList<Integer> keys = new ArrayList<>();
        for (String cloth : clothes)
            keys.add(clothingCodes.getKey(cloth.trim()));

        Collections.sort(keys);

        //Building the byte array.
        int numOfBytesNeeded = (int) Math.ceil(keys.get(keys.size()-1)/8.0);

        byte[] bytes = new byte[numOfBytesNeeded];
        for (Integer key : keys) {
            if (key == null)
                continue;

            byte b = 0b00000001;
            b = (byte) (b << ((key-1) % 8));
            bytes[numOfBytesNeeded - (key/8) - 1] |= b;         // We set the bytes in reverse order
        }

        return bytes;
    }

    public static int getHairStyleCodeFromString(String hairStyle) {
        Integer key = hairStyleCodes.getKey(hairStyle);
        return (key == null ? -1 : key);
    }

    public static int getBodyTypeCodeFromString(String bodyType) {
        Integer key = bodyTypeCodes.getKey(bodyType);
        return (key == null ? -1 : key);
    }

    public boolean hasAnyData() {
        return hairStyleCode != null ||
        bodyTypeCode != null ||
        clothingCode != null ||
        clothingColorList != null ||
        clothingColorAccurate != null ||
        hairColorList != null ||
        hairColorAccurate != null ||
        eyesColorList != null ||
        eyesColorAccurate != null ||
        skinColorList != null ||
        skinColorAccurate != null ||
        heightInCm != null;
    }

    /**
     * Creates a list with the values of the hash map.
     * This is needed for the adapter of the AutocompleteTextView.
     * @return  The list of values.
     */
    public static ArrayList<String> getHairStyleHashMapValues() {
        ArrayList<String> list = new ArrayList<>();
        if (hairStyleCodes != null)
            for (Map.Entry<Integer, String> e : hairStyleCodes.entrySet())
                list.add(e.getValue());

        return list;
    }

    /**
     * Creates a list with the values of the hash map.
     * This is needed for the adapter of the AutocompleteTextView.
     * @return  The list of values.
     */
    public static ArrayList<String> getBodyTypeHashMapValues() {
        ArrayList<String> list = new ArrayList<>();
        if (bodyTypeCodes != null)
            for (Map.Entry<Integer, String> e : bodyTypeCodes.entrySet())
                list.add(e.getValue());

        return list;
    }

    /**
     * Creates a list with the values of the hash map.
     * This is needed for the adapter of the AutocompleteTextView.
     * @return  A string array of values.
     */
    public static String[] getClothingHashMapValuesAsArray() {
        String[] array = null;
        if (clothingCodes != null) {
            int i = 0;
            array = new String[clothingCodes.size()];
            for (Map.Entry<Integer, String> e : clothingCodes.entrySet())
                array[i++] = e.getValue();
        }
        return array;
    }

    public static class CliqSearchFilter extends SearchFilter {
        private long lastUsedInMillis;
        private String filterName;
        private int filterNumber;

        private long userId;

        public CliqSearchFilter(Integer hairStyle, Integer bodyType,
                                byte[] clothing, Integer clothingColorList,
                                Integer clothingColorAccurate, Integer hairColorList,
                                Integer hairColorAccurate, Integer eyesColorList,
                                Integer eyesColorAccurate, Integer skinColorList,
                                Integer skinColorAccurate, Integer heightInCm,
                                boolean unitsInMetric,
                                long lastUsedInMillis,
                                String filterName,
                                int filterNumber, long userId) {

            super(hairStyle, bodyType, clothing, clothingColorList, clothingColorAccurate,
                    hairColorList, hairColorAccurate, eyesColorList, eyesColorAccurate,
                    skinColorList, skinColorAccurate, heightInCm, unitsInMetric);

            this.filterName = filterName;
            this.lastUsedInMillis = lastUsedInMillis;
            this.filterNumber = filterNumber;
            this.userId = userId;
        }

        public CliqSearchFilter(SearchFilter filter,
                                String filterName,
                                long userId) {
            super(filter.hairStyleCode, filter.bodyTypeCode, filter.clothingCode, filter.clothingColorList, filter.clothingColorAccurate,
                    filter.hairColorList, filter.hairColorAccurate, filter.eyesColorList, filter.eyesColorAccurate,
                    filter.skinColorList, filter.skinColorAccurate, filter.heightInCm, filter.unitsInMetric);
            this.filterName = filterName;
            this.lastUsedInMillis = -1;
            this.filterNumber = -1;
            this.userId = userId;
        }


        public long getLastUsedInMillis() {
            return lastUsedInMillis;
        }

        public String getFilterName() {
            return filterName;
        }

        public int getFilterNumber() {
            return filterNumber;
        }

        public long getUserId() {
            return userId;
        }
    }
}
