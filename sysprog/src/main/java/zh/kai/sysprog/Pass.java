package zh.kai.sysprog;

import java.util.ArrayList;
import java.util.HashMap;

import zh.kai.sysprog.Assembler.AdderssationType;
import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;
import zh.kai.sysprog.asm.Segment;
import zh.kai.sysprog.utils.Utils;

public class Pass {

    public static boolean first(
            ArrayList<Operation> operationsCodes,
            int lc,
            boolean hasError,
            CodeLine header,
            ArrayList<CodeLine> codeLines,
            HashMap<String,Address> symTab,
            HashMap<String,Address> externalLinks,
            ArrayList<String> externalSymbols,
            AdderssationType type
        ){
        for (int i = 1; i < codeLines.size(); i++) {//TODO переместить в Utils
            CodeLine currentLine = codeLines.get(i);

            if(!(lc<Assembler.WORD_MAX)){
                Errors.addPart1("Выход за границу адресного пространства");
                return false;
            }

            currentLine.setAddress(new Address(lc));
            String label = currentLine.getLabel();
            if(label!=null){
                if(!Utils.isValidLabel(label)){
                    Errors.addPart1("Некорректный формат метки: "+i+": "+currentLine);
                    hasError = true;
                }
                if(symTab.containsKey(label)){
                    Errors.addPart1("Дубликат метки на строке "+i+": "+currentLine);
                    hasError = true;
                }
                if(externalSymbols.contains(label)){
                    Errors.addPart1("Дубликат метки из extref на строке "+i+": "+currentLine);
                    hasError = true;
                }
                if(externalLinks.containsKey(label)){
                    externalLinks.put(label, currentLine.getAddress());
                }
                symTab.put(label, currentLine.getAddress());
                if(currentLine.getOperationName() == null){// проверка на строку метку
                    codeLines.remove(i);// удаление строки метки
                    i--;
                    continue;
                }
            }

            String operationName = currentLine.getOperationName();
            Operation currentOperation = getOperationByName(operationName,operationsCodes);
            if(currentOperation == null){
                if(Assembler.dirrectives.contains(operationName)){
                    if(operationName.startsWith("r") && !operationName.startsWith("re")){
                        Errors.addPart1("Имя операции "+operationName+" не может быть регистром: "+ currentLine);
                        return false;
                    }
                    switch (operationName) {
                        case "start" -> {
                            Errors.addPart1("Дирректива "+operationName+" не может быть использована лишь в начале программы: "+ currentLine);
                            return false;
                        }
                        case "segment" -> {

                        }
                        case "end" -> {
                            if(currentLine.getArguments().equals(header.getLabel())){
                                for(String s:externalLinks.keySet()){
                                    if(externalLinks.get(s)==null){
                                        Errors.addPart1("Ненайдена метка из extdef: "+s);
                                        return  false;
                                    }
                                }
                                return !hasError;
                            }else{
                                Errors.addPart1("Ожидается end "+header.getLabel());
                                return false;
                            }                        
                        }
                        case "extref" -> {
                            for(String s: currentLine.getArguments().split("\\s+")){
                                if(symTab.containsKey(s.trim())){
                                    
                                    Errors.addPart1("Дубликат метки в extref на строке "+i+": "+currentLine);
                                    hasError = true;
                                }
                                externalSymbols.add(s.trim());
                            }
                        }
                        case "extdef" -> {
                            for(String s: currentLine.getArguments().split("\\s+")){
                                externalLinks.put(s.trim(), null);
                            }
                        }
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
        if(!codeLines.getLast().getOperationName().equals("end") || !codeLines.getLast().getArguments().equals(header.getLabel())){
            Errors.addPart1("Ожидается end "+header.getLabel());
            return false;
        }
          return !hasError;

    }


    public static boolean second(
        ArrayList<Operation> operationsCodes,
        boolean hasError,
        CodeLine header,
        ArrayList<CodeLine> codeLines,
        HashMap<String,Address> symTab,
        HashMap<String,Address> externalLinks,
        ArrayList<String> externalSymbols,
        HashMap<Address,String> relocationTable,
        AdderssationType type
        
    ){

        for (int i = 1; i < codeLines.size(); i++) {//TODO переместить в Utils
            CodeLine currentLine = codeLines.get(i);
            String operationName = currentLine.getOperationName();
            if(Assembler.dirrectives.contains(operationName)){
                switch (operationName) {
                    case "end" -> {
                        String arguments = currentLine.getArguments();
                        int headerArg;
                        if(header.getArguments()==null){
                            headerArg = 0;
                        }else{
                            headerArg = Integer.parseInt(header.getArguments());
                        }
                        if(arguments != null){
                            int start = 0;
                            try{
                                String[] split = arguments.trim().split("\\s+");
                                if(split.length == 2){
                                    arguments = split[1];
                                    if(Utils.isIntegerRegex(arguments)) start = Integer.parseInt(arguments);
                                    else{
                                        Errors.addPart2("Некорректный аргумент end ожидается название программы и адрес");
                                        return false;
                                    }
                                }
                                short[] addres = Utils.intToShortArray4(start);
                                currentLine.setObj(new short[]{addres[1],addres[2],addres[3]});
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
                        
                        return !hasError;
                    }
                    case "resb", "resw" -> currentLine.setObj(new short[currentLine.getLenght()]);
                    case "word" -> {
                        int data = Integer.parseInt(currentLine.getArguments());
                        short[] obj = Utils.intToShortArray4(data);
                        currentLine.setObj(new short[]{obj[1],obj[2],obj[3]});
                    }
                    case "byte" -> {
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
                }
            }else if(getOperationByName(operationName,operationsCodes)!=null){ 
                Operation oper = getOperationByName(operationName,operationsCodes);
                short code = oper.getCode();
                code *= 4; // сдвиг на 2 влево
                String arguments = currentLine.getArguments();
                if(arguments == null){
                    currentLine.setObj(new short[]{code});
                }else if(arguments.startsWith("[")){
                    if(type == AdderssationType.DIRECT){
                        Errors.addPart1("Относительная адресация не поддерживается");
                        return false;
                    }
                    if(arguments.endsWith("]")){
                        arguments = arguments.substring(1, arguments.length()-1);
                        if(externalSymbols.contains(arguments)){
                            Errors.addPart2("Внешние ссылки не поддерживают относитльную адресацию");
                            return false;
                        }
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
                        Errors.addPart1("Прямая адресация не поддерживается");
                        return false;
                    }
                    if(currentLine.getLenght() == 4){
                        code += 1;//непосредственная
                        if(externalSymbols.contains(arguments)){
                            relocationTable.put(currentLine.getAddress(),arguments); 
                            currentLine.setObj(new short[]{code,0,0,0});
                        }else{
                            short[] addr = symTab.get(currentLine.getArguments()).toBytes();
                            currentLine.setObj(new short[]{code,addr[0],addr[1],addr[2]});
                            relocationTable.put(currentLine.getAddress(),""); //добавляем в таблицу релокации все прямые адресации 
                        }
                    }
                }else{
                    String[] split = arguments.split("\\s+");
                    switch (split.length) {
                        case 2 -> {
                            if(split[0].startsWith("r") && split[1].startsWith("r")){
                                code += 0; //регистровая
                                if(Assembler.dirrectives.contains(split[0]) && Assembler.dirrectives.contains(split[1])){
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
                                    relocationTable.put(currentLine.getAddress(),""); //добовляем в таблицу релокации все прямые адресации
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
        return false;
    }


    private static int dirrectiveLenght(CodeLine currentLine) {
        String operationName = currentLine.getOperationName();
        String arguments = currentLine.getArguments();
        switch (operationName) {
            case "word" -> {
                arguments = arguments.trim();
                if(Integer.parseInt(arguments)<Assembler.WORD_MAX &&Integer.parseInt(arguments)>=0){
                    return Assembler.WORD_LENGHT;
                }
                throw new NumberFormatException();
                
            }
            case "byte" -> {
                arguments = arguments.trim();
                if(arguments.startsWith("C\"") && arguments.endsWith("\"")){
                    return arguments.length()-3;
                }
                if(arguments.startsWith("X\"") && arguments.endsWith("\"")){
                    double a = (arguments.length()-3)/2.0;
                    double c = Math.ceil(a);
                    return (int) c;
                }
                if(Short.parseShort(arguments)<Assembler.MAX_BYTE && Short.parseShort(arguments)>=0){
                    return 1;
                }else{
                    throw new NumberFormatException();
                }
            }
            case "resb" -> {
                return Integer.parseInt(arguments);
            }
            case "resw" -> {
                return Integer.parseInt(arguments)*Assembler.WORD_LENGHT;
            }
            case "extref", "extdef","segment"-> {return 0;}
            default -> {
                throw new RuntimeException("nodef");
            }
        }
    }

    public static Operation getOperationByName(String oper,ArrayList<Operation> operationsCodes){
        if(operationsCodes.isEmpty()) return null;
        for(Operation op: operationsCodes){
            if(oper.equals(op.getName())) return op;
        }
        return null;
    }

}
