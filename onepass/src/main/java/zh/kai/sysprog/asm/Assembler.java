package zh.kai.sysprog.asm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Assembler {

    private String sourceCode;
    private String operationCodeTable;

    private ArrayList<CodeLine> codeLines;


    private ArrayList<String> errors;

    private boolean hasError = false;
    
    public static HashSet<String> dirrectives = new HashSet<>(List.of(
        "start","end",
        "extdef","extref","segment",
        "r0","r1","r2","r3","r4","r5","r6","r7",
        "r8","r9","r10","r11","r12","r13","r14","r15",
        "resb","resw","byte","word"
    )); 

    public Assembler(String srcCode, String opCodeTab) {
        sourceCode = srcCode;
        operationCodeTable = opCodeTab;
    }

    public void init(){
        errors = new ArrayList<>();
        codeLines = parseCode(sourceCode);
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
                switch (split.length) {
                    case 1 -> cls.add(new CodeLine(split[0], null, null));
                    case 2 -> cls.add(new CodeLine(split[0], split[1], null));
                    case 3 -> cls.add(new CodeLine(split[0], split[1], split[2]));
                }
            }else{
                split = line.split("\\s+",2);
                switch (split.length) {
                    case 1 -> cls.add(new CodeLine(null, split[1], null));
                    case 2 -> cls.add(new CodeLine(null, split[1], split[2]));
                }
            }
        }
    } catch (Exception e) {
        errors.add("Критическая ошибка");
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
                    operations.add(new Operation(name, code, lenght));
                }else{
                    errors.add("Не верный формат кода операции:" +nextLine);
                    hasError = true;
                }
            }            
        } catch (Exception e) {
            errors.add("Критическая ошибка");
            hasError = true;
        }
        return operations;
    }

}
