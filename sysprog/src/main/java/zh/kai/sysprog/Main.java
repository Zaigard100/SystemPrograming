package zh.kai.sysprog;

import java.util.Comparator;
import java.util.Map;

import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;

public class Main {

    static String text = """
    .prog start 100
    .pstart
        lda [.one]
        mov r1 r2
        lda [.two]
        add r1 r2
        sta [.res]
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
        end 101
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
            for(CodeLine cl:asm.getCodeLines()){
                System.out.println(cl.toAdditionString());
            }
            System.out.println("_______________________");
            
            System.out.println("Таблица символических имен:");
            asm.getSymTab().entrySet().stream()
                // 1. Сортируем по адресу (значению в Map.Entry)
                .sorted(Map.Entry.comparingByValue(
                    Comparator.comparing(s -> s.getAddress())
                ))
                // 2. Выводим результат в нужном формате
                .forEach(entry -> 
                    System.out.printf("%-12s %06X\n",
                        entry.getKey(), entry.getValue().getAddress()
                    )
            );
            System.out.println("_______________________");

            System.out.println("\tВторой проход:");
            if(asm.secondPass()){

                System.out.println("Таблица перемещений:");
                for(Address adr:asm.getRelocationTable()){
                    System.out.printf("%06X\n",adr.getAddress());
                }
                System.out.println("_______________________");
                System.out.println("Обьектный код:");
                for(CodeLine cl:asm.getCodeLines()){
                    System.out.println(cl.toObjString());
                }
                System.out.println("_______________________");
            }else{
                System.out.println(Errors.getPart2());
            }
        }else{
            System.out.println(Errors.getPart1());
        }

        System.out.println();
    }
}