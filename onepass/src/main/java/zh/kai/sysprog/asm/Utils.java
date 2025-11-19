package zh.kai.sysprog.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class Utils {

    public static final int WORD_LENGHT = 3;
    public static final int MAX_BYTE = 256;
    public static final int WORD_MAX = 16_777_216;

    public static short[] intToBin(int value){
        return new short[] {
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

    public static OptionalInt parseAndValidateArgument(String argument, AsmBlocks asm,CodeLine cl) {
        try {
            return OptionalInt.of(Integer.valueOf(argument));
        } catch (NumberFormatException e) {
            asm.addError("В аргументе ожидается число", cl);
            return OptionalInt.empty(); // Ошибка
        }
    }

    public static  String byteArrrayToString(short[] arr){
        StringBuilder sb = new StringBuilder();
        for(short b:arr){
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

}
