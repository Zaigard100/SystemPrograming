package zh.kai.sysprog.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Assembler extends AsmBlocks {

    private String sourceCodeString;
    private String operationCodeTableString;

    List<Segment> segments;
    boolean inSegment = false;

    private List<String> errors;

    private boolean hasError = false;

    int linePos = -1;
    
    public static HashSet<String> dirrectives = new HashSet<>(List.of(
        "start","end",
        "extdef","extref","segment",
        "r0","r1","r2","r3","r4","r5","r6","r7",
        "r8","r9","r10","r11","r12","r13","r14","r15",
        "resb","resw","byte","word"
    )); 

    public Assembler(String srcCode, String opCodeTab,AddressationType type) {
        super(type);
        sourceCodeString = srcCode;
        operationCodeTableString = opCodeTab;
    }

    public Assembler(String srcCode, String opCodeTab) {
        this(srcCode, opCodeTab, AddressationType.CHAINED);
    }
    @Override
    public void init(){
        super.init();
        errors = new ArrayList<>();
        segments = new ArrayList<>();
        codeLines = parseCode(sourceCodeString);
        operationsTable = parseOpCode(operationCodeTableString);

        linePos = 0;
        
    }
    
    public ArrayList<CodeLine> parseCode(String code){
        ArrayList<CodeLine> cls = new ArrayList<>();
    try (Scanner sc = new Scanner(code)) {
        while (sc.hasNext()) {
            String line =  sc.nextLine().trim();
            if(line.isBlank()) continue;
            String[] split;
            if(line.startsWith(".")){
                split = line.split("\\s+",3);
                CodeLine res = switch (split.length) {
                    case 1 -> (new CodeLine(split[0].trim(), null, null));
                    case 2 -> (new CodeLine(split[0].trim(), split[1].trim(), null));
                    case 3 -> (new CodeLine(split[0].trim(), split[1].trim(), split[2].trim()));
                    default -> null;
                };
                cls.add(res);
            }else{
                split = line.split("\\s+",2);
                CodeLine res = switch (split.length) {
                    case 1 -> (new CodeLine(null, split[0].trim(), null));
                    case 2 -> (new CodeLine(null, split[0].trim(), split[1].trim()));
                    default -> null;
                };
                cls.add(res);
            }
        }
    } catch (Exception e) {
        addError("Критическая ошибка",null);
        hasError = true;
    }
        return cls;
    }

    public ArrayList<Operation> parseOpCode(String opCodes){
        ArrayList<Operation> operations = new ArrayList<>();
        try (Scanner sc = new Scanner(opCodes)) {
            while (sc.hasNext()) {
                String nextLine = sc.nextLine();
                String[] split = nextLine.split("\\s+",3);
                if(split.length==3){
                    String name = split[0];
                    short code = -1;
                    int lenght = -1;
                    try {
                        code = Short.parseShort(split[1]);
                        lenght = Integer.parseInt(split[2]);
                    } catch (NumberFormatException e) {
                        errors.add("Не верный формат кода операции:" +nextLine);
                        hasError = true;
                    }

                    for(Operation o:operations){
                        if(o.getOperationName().equals(name)){
                            addError("Дубликат имени", null);
                        }
                        if(o.getCode() == code){
                            addError("Дубликат кода", null);
                        }                        
                    }

                    operations.add(new Operation(name, code, lenght));
                }else{
                    errors.add("Не верный формат кода операции:" +nextLine);
                    hasError = true;
                }
            }            
        } catch (Exception e) {
            
        addError("Критическая ошибка",null);   
            hasError = true;
        }
        return operations;
    }

    public boolean passStep(){
        boolean a;
        CodeLine cL = codeLines.get(linePos);
        if(cL.isSeg()) {
            segments.add(new Segment(type,new ArrayList<>()));
            segments.getLast().init();
            inSegment = true;
        }
        if(!inSegment){
            a = cL.pass(this);
            linePos++;
        }else{
            if(cL.isEnd()){
                if(cL.getArgument().equals(segments.getLast().getBlockName())){
                    inSegment = false;
                }else{
                    addError("Ожидался конец "+segments.getLast().getBlockName(), cL);
                }
            }
            a = cL.pass(segments.getLast());
            segments.getLast().getCodeLines().add(cL);
            codeLines.remove(linePos);
        }
        return a && !hasError;
    }

    public boolean passFull(){
        for(CodeLine cl:codeLines){
            if(cl.pass(this)){
                if(!cl.isLabelLine()) System.out.println(cl.toBin(this));
            }else{
                System.out.println();
                for(String s:errors){
                    System.out.println(s);
                }
                return false;
            }
        }
        return !hasError;
    }

    @Override
    public boolean addError(String err,CodeLine cl){
        hasError = true;
        errors.add(err+(cl==null?" ":cl.toString()));
        return false;
    }

    public String getSourseCode(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl: codeLines){
            sb.append(cl.toString()).append("\n");
        }
        return sb.toString();
    }  
    public String toBin(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl: codeLines){
            if(cl.isPassed){
                if(!cl.isLabelLine()) {
                    if(cl.isEnd()){
                        for(Address a: relocationTable.keySet()){
                            sb
                            .append("M ")
                            .append(a.toString())
                            .append(" ")
                            .append(relocationTable.get(a))
                            .append("\n");
                        }
                    }
                    sb.append(cl.toBin(this));       
                }
            }else{
                break;
            }
        }
        for (Segment s : segments) {
            for(CodeLine cl: s.getCodeLines()){
                if(cl.isPassed){
                    if(!cl.isLabelLine()) {
                        if(cl.isEnd()){
                            for(Address a: s.getRelocationTable().keySet()){
                                sb
                                .append("M ")
                                .append(a.toString())
                                .append(" ")
                                .append(s.getRelocationTable().get(a))
                                .append("\n");
                            }
                        }
                        sb.append(cl.toBin(s));       
                    }
                }else{
                    break;
                }
            }
        }
        return sb.toString();
    }

}
