package zh.kai.sysprog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.swing.text.Utilities;

import lombok.Getter;
import lombok.Setter;
import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;
import zh.kai.sysprog.asm.Segment;
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
    private HashMap<String,Address> symTab;
    private HashMap<Address,String> relocationTable;
    private HashMap<String,Address> externalLinks;
    private ArrayList<String> externalSymbols;
    private ArrayList<Segment> segments;

    private AdderssationType type;
    private boolean isFirstPass = false;
    private boolean isSecondPass = false;

    private String sourceCode;
    private String operationCodeTable;
    

    public static HashSet<String> dirrectives = new HashSet<>(List.of(
        "start","end",
        "extdef","extref","segment",
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
        sourceCode = code;
        operationCodeTable = operationCode;
        symTab = new HashMap<>();
        relocationTable = new HashMap<>();
        externalLinks = new HashMap<>();
        externalSymbols = new ArrayList<>();
        segments = new ArrayList<>();
    }

    public boolean  parseOperationsCodes(String text){
        operationsCodes = new ArrayList<>();
        String line = "";
        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNext()) {
                line  = sc.nextLine().trim();
                String[] split = line.split(" ", 3);
                if(split.length != 3){
                    Errors.addPart1("Неверный формат кода операции: "+ line);
                    return false;
                }
                String name = split[0];
                short c = Short.parseShort(split[1]);
                int lenght = Integer.parseInt(split[2]);

                if(lenght == 3 || lenght > 4){//добавлена длина команды 3 только для относительной адресации
                    Errors.addPart1("Неправильная длина команды допускаются только до 4: "+ line);
                    return false;
                }
                if(dirrectives.contains(name)){
                    Errors.addPart1(name + " зарезервировано: "+ line);
                    return false;
                }
                if(getOperationByName(name)!=null){
                    Errors.addPart1("Дубликат имени операции: "+ line);
                    return false;
                }
                if(c > 64 && c >= 0){
                    Errors.addPart1("Диапозон допустимых кодов операций от 0 до 63: "+ line);
                    return false;
                }
                if(getOperationByCode(c)!=null){
                    Errors.addPart1("Дубликат кода операции: "+ line);
                    return false;
                }
                
                operationsCodes.add(new Operation(name, c, lenght));
            }
        }catch(NumberFormatException e){
             Errors.addPart1("Неверный формат кода операции: "+ line);
             return false;
        }
        return true;
    }

    public boolean parseCode(String text){
        Segment currentSegment = null;
        codeLines = new ArrayList<>();
        segments = new ArrayList<>();
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

                if(currentSegment == null){
                    String programName = codeLines.getFirst().getLabel();
                    if("end".equals(last.getOperationName())){
                        if(last.getArguments() == null){
                            Errors.addPart1("Некорректный формат end");
                        }
                        if(last.getArguments().equals(programName)){
                            return true;
                        }else{
                            Errors.addPart1("Ожидался end "+codeLines.getFirst().getLabel());
                            return false;
                        }
                    }else if ("segment".equals(last.getOperationName())) {
                        if(last.getLabel() == null){
                            Errors.addPart1("некорректный формат сегмента");
                            return false;
                        }
                        currentSegment = new Segment(last.getLabel());
                        currentSegment.getCodeLines().add(last);
                        codeLines.removeLast();
                    }
                }else{
                    String segmentName = currentSegment.getName();
                    currentSegment.getCodeLines().add(last);
                    codeLines.removeLast();
                    if("end".equals(last.getOperationName())){
                        if(last.getArguments().equals(segmentName)){
                            segments.add(currentSegment);
                            currentSegment = null;
                        }
                    }else if ("segment".equals(last.getOperationName())) {
                        Errors.addPart1("Сегмент "+currentSegment.getName()+" не закрыт");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    

    public boolean firstPass(){

        if(!parseOperationsCodes(operationCodeTable)) return false;
        if(!parseCode(sourceCode)) return false;

        boolean hasError = false;
        CodeLine header = codeLines.get(0);
        if(!header.getOperationName().equals("start")){
            Errors.addPart1("Первой строкой ожидается дерректива start, было встречено: "+ header);
            return false;
        }

        int lc;
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
        if(lc != 0){
            Errors.addPart1("Не корректнаяя точка старта");
            return  false;
        }
        if(Pass.first(
            operationsCodes, 
            lc, 
            hasError, 
            header, 
            codeLines, 
            symTab, 
            externalLinks, 
            externalSymbols, 
            type
        )){
            for(Segment s:segments){
                if(!s.firstPass(this)){
                    Errors.addPart1("Ошибки в сегменте "+s.getName());
                    return  false;
                }
            }
        }else{
            Errors.addPart1("Ошибки в программе");
            return false;
        }
        isFirstPass = true;
        return true;

    }

    public boolean secondPass(){
        boolean hasError = false;
        CodeLine header = codeLines.get(0);
        if(Pass.second(
            operationsCodes, 
            hasError, 
            header, 
            codeLines, 
            symTab, 
            externalLinks, 
            externalSymbols, 
            relocationTable, 
            type
        )){
            for(Segment s:segments){
                if(!s.secondPass(this)){
                    Errors.addPart1("Ошибки в сегменте "+s.getName());
                    return  false;
                }
            }
        }

        return !hasError;
    }

    public void saveToFile(){
        String filename = codeLines.getFirst().getLabel().substring(1)+".obj";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(getObjText()); // Запись текста в файл
            System.out.println("Текст успешно сохранен в файл: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    public String getAuxiliaryTable(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl:getCodeLines()){
            sb.append(cl.toAdditionString()).append("\n");
        }
        for(Segment s:segments){
            sb.append(s.getAuxiliaryTable());
        }
        return sb.toString().trim();
    }

    public String getObjText(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl:getCodeLines()){
            if(cl.isHead()){
                sb.append(cl.toObjString()).append("\n");
            }else if (cl.isExtdef()) {
                String[] defs = cl.getArguments().trim().split(" ");
                for(String s:defs){
                    sb.append(String.format("D %s %06X \n",s,symTab.get(s).getAddress()));
                }
            }else if (cl.isExtref()) {
                String[] defs = cl.getArguments().trim().split(" ");
                for(String s:defs){
                    sb.append("R ").append(s).append("\n");
                }
            }else if(cl.isEnd()){
                for(Address adr: relocationTable.keySet()){
                    String format = String.format("M %06X %s", adr.getAddress(),relocationTable.get(adr));
                    sb.append(format).append("\n");
                }
                sb.append(cl.toObjString()).append("\n");
            }else{
                sb.append(cl.toObjString()).append("\n");
            }
        }
        for(Segment s:segments){
            sb.append(s.getObjText()).append("\n");
        }
        return sb.toString().trim();
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
