package zh.kai.sysprog;

import java.util.Comparator;
import java.util.Map;

import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;
import zh.kai.sysprog.asm.Segment;

public class Main {

    static String text = """
    .prog start
        extdef .data .res
        extref .key .hash
    .pstart
        lda [.one]
        mov r1 r2
        sta .hash
        lda .two
        add r1 r2
        sta [.res]
        lda .key
        int 29
        clr
        jmp [.pstart]
        jmp .pstart
        resb 4
    .data
    .bighex byte X"FAF09"
        resw 5
    .one byte 1
    .two byte 2
    .res word 34
    .hello byte C"Hello"

    .s1 segment
        extdef .key
        extref .res
    .ff lda .res
        int 29
    .key byte C"KEYGEN"
    end .s1

    .s2 segment
        extdef .hash
        extref .data
    .ff sta .data
        add r14 r8
    .hash byte X"F32"
    end .s2
    
        end .prog
    """;

    static String opcod = """
    add 1 2
    lda 2 4
    sta 3 4
    int 4 2
    clr 5 1
    mov 6 2
    jmp 7 4
    """;

    public static void main(String[] args) {


        Assembler asm = new Assembler(text,opcod,Assembler.AdderssationType.CHAINED);
        asm.init();
        asm.parseCode(text);
        asm.parseOperationsCodes(opcod);
        System.out.println("Тип адресации: "+asm.getType());

        System.out.println("Исходный текст:");
        for(CodeLine cl:asm.getCodeLines()){
                String a1 = cl.getLabel();
                if(a1 == null){
                    a1 = "";
                }
                String a2 = cl.getOperationName();
                if(a2 == null){
                    a2 = "";
                }
                String a3 = cl.getArguments();
                if(a3 == null){
                    a3 = "";
                }
                System.out.printf( "%-12s %s %s\n",
                    a1,
                    a2,
                    a3
                );
            }
            for(Segment s: asm.getSegments()){
                for(CodeLine cl:s.getCodeLines()){
                String a1 = cl.getLabel();
                if(a1 == null){
                    a1 = "";
                }
                String a2 = cl.getOperationName();
                if(a2 == null){
                    a2 = "";
                }
                String a3 = cl.getArguments();
                if(a3 == null){
                    a3 = "";
                }
                System.out.printf( "%-12s %s %s\n",
                    a1,
                    a2,
                    a3
                );
            }
            }
        System.out.println("_______________________");

        System.out.println("Таблица кодов:");
        for(Operation cl:asm.getOperationsCodes()){
                System.out.printf( "%-8s %s %s\n",
                    cl.getName(),
                    cl.getCode(),
                    cl.getLenght()
                );
            }
        System.out.println("_______________________");

        System.out.println("\tПервый проход:");

        if(asm.firstPass()){
            System.out.println("Вспомогательная таблица:");
            System.out.println(asm.getAuxiliaryTable());
            System.out.println("_______________________");
            
            System.out.println("Таблица символических имен:");
            asm.getSymTab().entrySet().stream()
                // 1. Сортируем по адресу (значению в Map.Entry)
                .sorted(Map.Entry.comparingByValue(
                    Comparator.comparing(a -> a.getAddress())
                ))
                // 2. Выводим результат в нужном формате
                .forEach(entry -> 
                    System.out.printf("%-12s %06X %s %s\n",
                        entry.getKey(), 
                        entry.getValue().getAddress(),
                        asm.getExternalLinks().keySet().contains(entry.getKey())?1:0,
                        asm.getCodeLines().getFirst().getLabel()
                    )
            );
            for(Segment s:asm.getSegments()){
                s.getSymTab().entrySet().stream()
                    // 1. Сортируем по адресу (значению в Map.Entry)
                    .sorted(Map.Entry.comparingByValue(
                        Comparator.comparing(a -> a.getAddress())
                    ))
                    // 2. Выводим результат в нужном формате
                    .forEach(entry -> 
                        System.out.printf("%-12s %06X %s %s\n",
                            entry.getKey(), 
                            entry.getValue().getAddress(),
                            asm.getExternalLinks().keySet().contains(entry.getKey())?1:0,
                            s.getName()
                        )
                );
            }
            System.out.println("_______________________");

            System.out.println("\tВторой проход:");
            if(asm.secondPass()){

                System.out.println("Таблица перемещений:");
                for(Address adr:asm.getRelocationTable().keySet()){
                    System.out.printf("%06X %-15s %s\n",
                        adr.getAddress(),
                        asm.getRelocationTable().get(adr),
                        asm.getCodeLines().getFirst().getLabel()
                    );
                }
                for(Segment s:asm.getSegments()){
                    for(Address adr:s.getRelocationTable().keySet()){
                        System.out.printf("%06X %-15s %s\n",
                            adr.getAddress(),
                            s.getRelocationTable().get(adr),
                            s.getName()
                        );
                    }
                }
                System.out.println("_______________________");

                System.out.println("Таблица внешних ссылок:");
                for(String l:asm.getExternalLinks().keySet()){
                    System.out.printf("%-12s %06X %s\n",
                        l,
                        asm.getExternalLinks().get(l).getAddress(),
                        asm.getCodeLines().getFirst().getLabel()
                    );
                }
                for(Segment s:asm.getSegments()){
                    for(String l:s.getExternalLinks().keySet()){
                        System.out.printf("%-12s %06X %s\n",
                            l,
                            s.getExternalLinks().get(l).getAddress(),
                            s.getName()
                        );
                    }
                }
                System.out.println("_______________________");
                System.out.println("Обьектный код:");
                System.out.println(asm.getObjText());
                System.out.println("_______________________");
                asm.saveToFile();
            }else{
                System.out.println(Errors.getPart2());
            }
        }else{
            System.out.println(Errors.getPart1());
        }

        System.out.println();
    }
}