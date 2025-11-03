package zh.kai.sysprog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import lombok.Setter;
import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;
import zh.kai.sysprog.utils.Utils;

@Getter
@Setter
public class Assembler {

    public enum AdderssationType{
        DIRECT("Прямая"), //прямая
        RELATIVE("Относительная"), //относительная
        CHAINED("Смешанная"); //смешанная
        
        String typeName;

        AdderssationType(String typeName){
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        

    }

    public static final int WORD_LENGHT = 3;
    public static final int MAX_BYTE = 256;
    public static final int WORD_MAX = 16_777_216;
    private ArrayList<Operation> operationsCodes;
    private ArrayList<CodeLine> codeLines;
    private ArrayList<Address> relocationTable;
    private HashMap<String, Address> symTab;

    private AdderssationType type;
    private boolean isFirstPass = false;
    private boolean isSecondPass = false;

    private String sourceCode;
    private String operationCodeTable;
    

    public static HashSet<String> dirrectives = new HashSet<>(List.of(
        "start","end",
        "r0","r1","r2","r3","r4","r5","r6","r7",
        "r8","r9","r10","r11","r12","r13","r14","r15",
        "resb","resw","byte","word"
    ));


    
    public Assembler(String text, String opCodes,AdderssationType type){
        this.type = type;
        sourceCode = text;
        operationCodeTable = opCodes;
    }
    
    public void init(){
        init(sourceCode,operationCodeTable);
    }

    public void init(String code,String operationCode){
        parseOperationsCodes(operationCode);
        parseCode(code);
        symTab = new HashMap<>();
        relocationTable = new ArrayList<>();
    }

    public void parseOperationsCodes(String text){
        operationsCodes = new ArrayList<>();
        String line = "";
        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNext()) {
                line  = sc.nextLine().trim();
                String[] split = line.split(" ", 3);
                if(split.length != 3){
                    Errors.addPart1("Неверный формат кода операции: "+ line);
                    continue;
                }
                String name = split[0];
                short c = Short.parseShort(split[1]);
                int lenght = Integer.parseInt(split[2]);

                if(lenght == 3 || lenght > 4){//добавлена длина команды 3 только для относительной адресации
                    Errors.addPart1("Неправильная длина команды допускаются только до 4: "+ line);
                }
                if(dirrectives.contains(name)){
                    Errors.addPart1(name + " зарезервировано: "+ line);
                    continue;
                }
                if(getOperationByName(name)!=null){
                    Errors.addPart1("Дубликат имени операции: "+ line);
                    continue;
                }
                if(c > 64 && c >= 0){
                    Errors.addPart1("Диапозон допустимых кодов операций от 0 до 63: "+ line);
                    continue;
                }
                if(getOperationByCode(c)!=null){
                    Errors.addPart1("Дубликат кода операции: "+ line);
                    continue;
                }
                
                operationsCodes.add(new Operation(name, c, lenght));
            }
        }catch(NumberFormatException e){
             Errors.addPart1("Неверный формат кода операции: "+ line);
        }

    }

    public void parseCode(String text){
        codeLines = new ArrayList<>();
        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNext()) {
               String line = sc.nextLine().trim();
               if(line.isBlank()) continue;
               String[] split;
               if(line.startsWith(".")){
                    split = line.split(" ",3);
                    switch (split.length) {
                       case 3 -> codeLines.add(new CodeLine(split[0], split[1],split[2]));
                       case 2 -> codeLines.add(new CodeLine(split[0], split[1], null)); //без аргументов
                       case 1 -> codeLines.add(new CodeLine(split[0], null, null));//строка метка
                   }
               }else{
                    split = line.split(" ",2);
                   switch (split.length) {
                       case 2 -> codeLines.add(new CodeLine(null, split[0],split[1]));
                       case 1 -> codeLines.add(new CodeLine(null, split[0], null)); //без аргументов
                   }
               }
               
               CodeLine last = codeLines.getLast();
               if("end".equals(last.getOperationName())){
                return;
               }
            }
        }
    }

    public boolean firstPass(){
        boolean hasError = false;
        CodeLine header = codeLines.get(0);
        if(!header.getOperationName().equals("start")){
            Errors.addPart1("Первой строкой ожидается дерректива start, было встречено: "+ header);
            return false;
        }

        int lc = -1;
        if(header.getArguments()!= null){
            try{
                lc = Integer.parseInt(header.getArguments()); //заполняем начальный адрес LOCCTR
            }catch(NumberFormatException e){
                Errors.addPart1("Аргумент дерректива start должен быть адрес: "+ header);
                return false;
            }
        }else{
            header.setArguments("0");
            lc = 0;
        }
        if(lc < 0){
            Errors.addPart1("Не корректнаяя точка старта");
            hasError = true;
        }

        for (int i = 1; i < codeLines.size(); i++) {
            CodeLine currentLine = codeLines.get(i);

            if(!(lc<WORD_MAX)){
                Errors.addPart1("Выход за границу адресного пространства");
                return false;
            }

            currentLine.setAddress(new Address(lc));
            if(currentLine.getLabel()!=null){
                if(!Utils.isValidLabel(currentLine.getLabel())){
                    Errors.addPart1("Некорректный формат метки: "+i+": "+currentLine);
                    hasError = true;
                }
                if(symTab.containsKey(currentLine.getLabel())){
                    Errors.addPart1("Дубликат метки на строке "+i+": "+currentLine);
                    hasError = true;
                }
                symTab.put(currentLine.getLabel(), new Address(lc));
                if(currentLine.getOperationName() == null){// проверка на строку метку
                    codeLines.remove(i);// удаление строки метки
                    i--;
                    continue;
                }
            }

            String operationName = currentLine.getOperationName();
            Operation currentOperation = getOperationByName(operationName);
            if(currentOperation == null){
                if(dirrectives.contains(operationName)){
                    if(operationName.startsWith("r") && !operationName.startsWith("re")){
                        Errors.addPart1("Имя операции "+operationName+" не может быть регистром: "+ currentLine);
                        return false;
                    }
                    if(operationName.equals("start")){
                        Errors.addPart1("Дирректива "+operationName+" не может быть использована лишь в начале программы: "+ currentLine);
                        return false;
                    }
                    if(operationName.equals("end")){
                        isFirstPass = !hasError;
                        return !hasError;
                    }
                    try{
                        int lenght;
                        try{
                            lenght = dirrectiveLenght(currentLine);
                        }catch(NumberFormatException e){
                            Errors.addPart1("Ошибка формата числа");
                            return false;
                        }
                        currentLine.setLenght(lenght);
                        if(!currentLine.checkLenght(type)){
                            hasError = true;
                        }
                        lc += lenght;
                    }catch(NumberFormatException e){
                        Errors.addPart1("Не верный формат диррективы: "+ currentLine);
                        return false;
                    }
                }else{
                    Errors.addPart1("Имя операции "+operationName+" не определено: "+ currentLine);
                    return false;
                }
            }else{
                int lenght = currentOperation.getLenght();
                currentLine.setLenght(lenght);
                
                if(!currentLine.checkLenght(type)){
                    hasError = true;
                }
                lc += lenght;
            }
        }
        if(!codeLines.getLast().getOperationName().equals("end")){
            Errors.addPart1("Ожидается end");
            return false;
        }

        isFirstPass = !hasError;
        return !hasError;

    }

    public boolean secondPass(){
        boolean hasError = false;
        CodeLine header = codeLines.get(0);

        for (int i = 1; i < codeLines.size(); i++) {
            CodeLine currentLine = codeLines.get(i);
            String operationName = currentLine.getOperationName();
            if(dirrectives.contains(operationName)){
                if(operationName.equals("end")){
                    String arguments = currentLine.getArguments();
                    int headerArg = Integer.parseInt(header.getArguments());
                    if(arguments != null){
                        int start;
                        try{
                        start = Integer.parseInt(arguments);
                        if(start >= headerArg && start<= currentLine.getAddress().getAddress()){
                            short[] addres = Utils.intToShortArray4(start);
                            currentLine.setObj(new short[]{addres[1],addres[2],addres[3]});
                        }else{
                            Errors.addPart2("Некорректный аргумент end адрес должен находится в диапозоне кода");
                            hasError = true;
                        }
                        }catch(NumberFormatException e){
                            Errors.addPart2("Некорректный аргумент end ожидается адрес");
                            return false;
                        }
                    }else{
                        int start = headerArg;
                        short[] addres = Utils.intToShortArray4(start);
                        currentLine.setObj(new short[]{addres[1],addres[2],addres[3],});
                    }
                    short[] addres = Utils.intToShortArray4(headerArg);
                    short[] length = Utils.intToShortArray4(currentLine.getAddress().getAddress() - headerArg);
                    header.setObj(new short[]{addres[1],addres[2],addres[3],length[1],length[2],length[3]});

                    isSecondPass = !hasError;
                    return !hasError;
                }
                if(operationName.equals("resb") || operationName.equals("resw") ){
                    currentLine.setObj(new short[currentLine.getLenght()]);
                }
                if(operationName.equals("word")){
                    int data = Integer.parseInt(currentLine.getArguments());
                    short[] obj = Utils.intToShortArray4(data);
                    currentLine.setObj(new short[]{obj[1],obj[2],obj[3]});
                }
                if(operationName.equals("byte")){
                    String arguments = currentLine.getArguments().trim();
                    if(arguments.startsWith("C\"") && arguments.endsWith("\"")){
                        try{
                            short[] data = Utils.stringToAsciiShortArray(arguments.substring(2, arguments.length()-1));
                            currentLine.setObj(data);
                        }catch(Exception e){
                            if(e.getMessage().equals("noascii")){
                                Errors.addPart2("Встречен не ASCII символ: " + arguments.substring(2, arguments.length()-1));
                                hasError = true;
                            }
                        }
                    }else if(arguments.startsWith("X\"") && arguments.endsWith("\"")){
                        String hexString = arguments.substring(2, arguments.length()-1);
                        if(Utils.isHex(hexString)){
                            short[] hex = Utils.hexStringToShortArray(hexString);
                            currentLine.setObj(hex);
                        }else{
                            Errors.addPart2("Ожидалось hex: "+ hexString);
                        }
                    }else{
                        int data = Integer.parseInt(currentLine.getArguments());
                        short[] obj = Utils.intToShortArray4(data);
                        currentLine.setObj(new short[]{obj[3]});
                    }
                }
            }else if(getOperationByName(operationName)!=null){ 
                Operation oper = getOperationByName(operationName);
                short code = oper.getCode();
                code *= 4; // сдвиг на 2 влево
                String arguments = currentLine.getArguments();
                if(arguments == null){
                    currentLine.setObj(new short[]{code});
                }else if(arguments.startsWith("[")){
                    if(type == AdderssationType.DIRECT){
                        Errors.addPart1("Относительная адресация не поддерживается: "+this);
                        return false;
                    }
                    if(arguments.endsWith("]")){
                        arguments = arguments.substring(1, arguments.length()-1);
                        code += 2;//относительная
                        if(arguments.startsWith(".")){
                            //для относительной адрессации
                            int nextLineAddress = currentLine.getAddress().getAddress() + currentLine.getLenght();
                            int labelAddress = symTab.get(currentLine.getArguments().substring(1, currentLine.getArguments().length()-1)).getAddress();
                            int relativeAddres = labelAddress - nextLineAddress;
                            short[] addr = Utils.intToShortArray4(relativeAddres);
                            currentLine.setObj(new short[]{code,addr[1],addr[2],addr[3]});
                        }else if(Utils.isIntegerRegex(arguments)){
                            //для относительной адрессации
                            int nextLineAddress = currentLine.getAddress().getAddress() + currentLine.getLenght();
                            int argumentAddress = Integer.parseInt(arguments);
                            int relativeAddres = argumentAddress - nextLineAddress;
                            short[] addr = Utils.intToShortArray4(relativeAddres);
                            currentLine.setObj(new short[]{code,addr[1],addr[2],addr[3]});
                        }
                    }else{
                        Errors.addPart1("Некорректный аргумент");
                        return false;
                    }
                }else if(arguments.startsWith(".")){
                    if(type == AdderssationType.RELATIVE){
                        Errors.addPart1("Прямая адресация не поддерживается: "+this);
                        return false;
                    }
                    if(currentLine.getLenght() == 4){
                        code += 1;//непосредственная
                        short[] addr = symTab.get(currentLine.getArguments()).toBytes();
                        //addr[0] = code;
                        currentLine.setObj(new short[]{code,addr[0],addr[1],addr[2]});
                        relocationTable.add(currentLine.getAddress()); //добовляем в таблицу релокации все прямые адресации 
                    }
                }else{
                    String[] split = arguments.split(" ");
                    switch (split.length) {
                        case 2 -> {
                            if(split[0].startsWith("r") && split[1].startsWith("r")){
                                code += 0; //регистровая
                                if(dirrectives.contains(split[0]) && dirrectives.contains(split[1])){
                                    short regs = Short.parseShort(split[0].substring(1));
                                    regs = (short) (regs * 16);
                                    regs += Short.parseShort(split[1].substring(1));
                                    currentLine.setObj(new short[]{code,regs});
                                }else{
                                    Errors.addPart2("Доступны регистры от r0 до r15");
                                }
                            }else{
                                Errors.addPart2("Ожидалося регистр: " + currentLine);
                                return false;
                            }
                        }
                        case 1 -> {
                        switch (currentLine.getLenght()) {
                            case 4 ->                                 {
                                    code += 1;//непосредственная
                                    short[] addr = Utils.intToShortArray4(Integer.parseInt(arguments));
                                    addr[0] = code;
                                    relocationTable.add(currentLine.getAddress()); //добовляем в таблицу релокации все прямые адресации
                                    currentLine.setObj(addr);
                                }
                            case 2 -> {
                                code += 1; //непосредственная
                                short[] val = Utils.intToShortArray4(Integer.parseInt(arguments));
                                val[0] = code;
                                currentLine.setObj(new short[]{code,val[3]});
                            }
                            default -> {
                            }
                        }
                        }
                        default -> {
                            Errors.addPart2("Некорректный формат агргумента: " + currentLine);
                            return false;
                        }
                    }
                }
            }else{
                Errors.addPart2("Мнемоника операции или дерректива не найдены: "+ currentLine);
                return false;
            }
        }
        isSecondPass = !hasError;
        return !hasError;
    }

    private int dirrectiveLenght(CodeLine currentLine) {
        String operationName = currentLine.getOperationName();
        String arguments = currentLine.getArguments().trim();
        switch (operationName) {
            case "word" -> {
                if(Integer.parseInt(arguments)<WORD_MAX &&Integer.parseInt(arguments)>=0){
                    return WORD_LENGHT;
                }
                throw new NumberFormatException();
                
            }
            case "byte" -> {
                if(arguments.startsWith("C\"") && arguments.endsWith("\"")){
                    return arguments.length()-3;
                }
                if(arguments.startsWith("X\"") && arguments.endsWith("\"")){
                    double a = (arguments.length()-3)/2.0;
                    double c = Math.ceil(a);
                    return (int) c;
                }
                if(Short.parseShort(arguments)<MAX_BYTE && Short.parseShort(arguments)>=0){
                    return 1;
                }else{
                    throw new NumberFormatException();
                }
            }
            case "resb" -> {
                return Integer.parseInt(arguments);
            }
            case "resw" -> {
                return Integer.parseInt(arguments)*WORD_LENGHT;
            }
            default -> {
                throw new RuntimeException("nodef");
            }
        }
    }

    public Operation getOperationByName(String oper){
        if(operationsCodes.isEmpty()) return null;
        for(Operation op: operationsCodes){
            if(oper.equals(op.getName())) return op;
        }
        return null;
    }

    public Operation getOperationByCode(short code){
        if(operationsCodes.isEmpty()) return null;
        for(Operation op: operationsCodes){
            if(code == op.getCode()) return op;
        }
        return null;
    }

}
