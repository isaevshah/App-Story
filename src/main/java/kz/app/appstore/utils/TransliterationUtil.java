package kz.app.appstore.utils;
import java.util.HashMap;
import java.util.Map;

public class TransliterationUtil {

    private static final Map<Character, String> translitMap = new HashMap<>();

    static {
        translitMap.put('а', "a");  translitMap.put('б', "b");
        translitMap.put('в', "v");  translitMap.put('г', "g");
        translitMap.put('д', "d");  translitMap.put('е', "e");
        translitMap.put('ё', "yo"); translitMap.put('ж', "zh");
        translitMap.put('з', "z");  translitMap.put('и', "i");
        translitMap.put('й', "y");  translitMap.put('к', "k");
        translitMap.put('л', "l");  translitMap.put('м', "m");
        translitMap.put('н', "n");  translitMap.put('о', "o");
        translitMap.put('п', "p");  translitMap.put('р', "r");
        translitMap.put('с', "s");  translitMap.put('т', "t");
        translitMap.put('у', "u");  translitMap.put('ф', "f");
        translitMap.put('х', "kh"); translitMap.put('ц', "ts");
        translitMap.put('ч', "ch"); translitMap.put('ш', "sh");
        translitMap.put('щ', "shch"); translitMap.put('ъ', "");
        translitMap.put('ы', "y");  translitMap.put('ь', "");
        translitMap.put('э', "e");  translitMap.put('ю', "yu");
        translitMap.put('я', "ya");

        // Поддержка казахских букв (опционально)
        translitMap.put('ә', "a");  translitMap.put('ғ', "g");
        translitMap.put('қ', "q");  translitMap.put('ң', "n");
        translitMap.put('ө', "o");  translitMap.put('ү', "u");
        translitMap.put('ұ', "u");  translitMap.put('h', "h");
    }

    public static String transliterate(String input) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toLowerCase().toCharArray()) {
            result.append(translitMap.getOrDefault(ch, String.valueOf(ch)));
        }
        return result.toString();
    }
}

