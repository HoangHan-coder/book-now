package vn.edu.fpt.booknow.utils;

import java.text.Normalizer;

public class TextUtils {

    public static String removeAccent(String input) {
        if (input == null) return null;

        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}", "").toLowerCase();
    }
}