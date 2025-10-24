package zh.kai.sysprog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;

public class Assembler {

    ArrayList<Operation> operationsCodes;
    ArrayList<CodeLine> codeLines;
    HashMap<String, Integer> symTab;

    HashSet<String> dirrectives = new HashSet<>(List.of(
        "start","end",
        "r0","r1","r2","r3","r4","r5","r6","r7",
        "r8","r9","r10","r11","r12","r13","r14","r15",
        "resb","resw","byte","word"
    ));


    
    public Assembler(){
        parseOperationsCodes(Main.opcod);
        parseCode(Main.text);
        symTab = new HashMap<>();
    }
    
    public Assembler(String code,String operationCode){
        parseOperationsCodes(operationCode);
        parseCode(code);
        symTab = new HashMap<>();
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

                if(lenght == 3 || lenght > 4){
                    Errors.addPart1("Неправильная длина команды допускаются только 1,2,4: "+ line);
                }
                if(dirrectives.contains(name)){
                    Errors.addPart1(name + " зарезервировано: "+ line);
                    continue;
                }
                if(getOperationByName(name)!=null){
                    Errors.addPart1("Дубликат имени операции: "+ line);
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
            lc = Integer.parseInt(header.getArguments()); //заполняем начальный адрес LOCCTR
        }else{
            Errors.addPart1("Неопределена точка старта программы");
            hasError = true;
        }
        if(lc <= 0){
            Errors.addPart1("Точка старта программы не может быть больше 0");
            hasError = true;
        }

        for (int i = 1; i < codeLines.size(); i++) {
            CodeLine currentLine = codeLines.get(i);
            currentLine.setAddress(lc);
            if(currentLine.getLabel()!=null){
                if(symTab.containsKey(currentLine.getLabel())){
                    Errors.addPart1("Дубликат метки на строке "+i+": "+currentLine);
                    hasError = true;
                }
                symTab.put(currentLine.getLabel(), lc);
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
                        return true;
                    }
                    try{
                        lc += dirrectiveLenght(currentLine);
                    }catch(NumberFormatException e){
                        Errors.addPart1("Не верный формат диррективы: "+ currentLine);
                        return false;
                    }
                }else{
                    Errors.addPart1("Имя операции "+operationName+" не определено: "+ currentLine);
                    return false;
                }
            }else{
                lc += currentOperation.getLenght();
            }
        }

        return !hasError;

    }

    private int dirrectiveLenght(CodeLine currentLine) {
        String operationName = currentLine.getOperationName();
        String arguments = currentLine.getArguments().trim();
        switch (operationName) {
            case "word" -> {
                return 3;
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
                if(Short.parseShort(arguments)<256){
                    return 1;
                }else{
                    throw new NumberFormatException();
                }
            }
            case "resb" -> {
                return Integer.parseInt(arguments);
            }
            case "resw" -> {
                return Integer.parseInt(arguments)*3;
            }
            default -> {
                throw new NumberFormatException();
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
