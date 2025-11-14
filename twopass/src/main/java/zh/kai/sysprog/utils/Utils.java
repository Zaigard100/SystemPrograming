package zh.kai.sysprog.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isIntegerRegex(String str) {
        if (str == null) {
            return false;
        }
        // Шаблон: необязательный минус (-?), одна или более цифр (\d+)
        return str.matches("-?\\d+"); 
    }

    public static short[] intToShortArray4(int value) {
        return new short[] {
            (short) ((value >> 24) & 0xFF), // 0x00 (или 0x0000)
            (short) ((value >> 16) & 0xFF), // 0x02 (или 0x0002)
            (short) ((value >> 8) & 0xFF),  // 0x56 (или 0x0056)
            (short) (value & 0xFF)          // 0xAF (или 0x00AF)
        };
    }

    public static short[] stringToAsciiShortArray(String inputString) {
        if (inputString == null) {
            return new short[0];
        }

        int length = inputString.length();
        short[] asciiArray = new short[length];

        for (int i = 0; i < length; i++) {
            // char в Java - это 16-битное (UTF-16) беззнаковое значение.
            // Приведение (short) сохраняет это значение.
            // Если символ находится в диапазоне ASCII (0-127), это его ASCII-код.
            // Если символ находится за пределами диапазона ASCII, это его UTF-16 код.
            
            char charAt = inputString.charAt(i);
            if(charAt<128){
                asciiArray[i] = (short) charAt;
            }else{
                throw new RuntimeException("noascii");
            }
        }
        return asciiArray;
    }

    public static short[] hexStringToShortArray(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new short[0];
        }

        // 1. Преобразование в верхний регистр и удаление пробелов
        String cleanHex = hexString.toUpperCase().trim();
        
        // 2. Дополнение строки нулем, если она нечетной длины
        // "FAF4C" -> "0FAF4C"
        if (cleanHex.length() % 2 != 0) {
            cleanHex = "0" + cleanHex;
        }

        int len = cleanHex.length();
        List<Short> shortList = new ArrayList<>();

        // 3. Обработка по два символа
        for (int i = 0; i < len; i += 2) {
            String byteStr = cleanHex.substring(i, i + 2);
            
            // 4. Парсинг двух символов как одного байта (шестнадцатеричного числа)
            // Используем Integer.parseInt() с основанием 16
            // Результат будет от 0 до 255.
            int byteValue = Integer.parseInt(byteStr, 16);
            
            // 5. Сохранение в массив short.
            // Приведение к short безопасно, т.к. значение не превышает 255.
            shortList.add((short) byteValue);
        }

        // 6. Преобразование List<Short> в short[]
        short[] result = new short[shortList.size()];
        for (int i = 0; i < shortList.size(); i++) {
            result[i] = shortList.get(i);
        }

        return result;
    }
    public static boolean isHex(String s) {
        // Проверяем, что строка не null и имеет четную длину (для полной байтовой записи)
        if (s == null) {
            return false;
        }
        // Используем регулярное выражение: ^[0-9A-Fa-f]+$
        // ^ и $ - начало и конец строки; [0-9A-Fa-f] - любой HEX символ; + - один или более.
        return s.matches("^[0-9A-Fa-f]+$");
    }

    public static boolean isValidLabel(String label) {
        String regex = "^\\.\\w+$";
        return label.matches(regex);
    }

}
