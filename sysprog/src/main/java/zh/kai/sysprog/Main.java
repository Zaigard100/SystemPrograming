package zh.kai.sysprog;

import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Operation;

public class Main {

    static final String text = """
    .prog start 100
        ldar1 .one
        mov r1 r2
        ldar1 .two
        add r1 r2
        star1 .res
        int 29
        clr
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

    static final String opcod = """
    add 1 2
    ldar1 2 4
    star1 3 4
    int 4 2
    clr 5 1
    mov 6 2
    """;

    public static void main(String[] args) {

        Assembler asm = new Assembler();

        System.out.println("Исходный текст:");
        for(CodeLine cl:asm.codeLines){
                System.out.printf( "%-12s %s %s\n",
                    cl.getLabel(),
                    cl.getOperationName(),
                    cl.getArguments()
                );
            }
        System.out.println("_______________________");

        System.out.println("Таблица кодов:");
        for(Operation cl:asm.operationsCodes){
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
            for(CodeLine cl:asm.codeLines){
                System.out.println(cl.toAdditionString());
            }
            System.out.println("_______________________");
            
            System.out.println("Таблица символических имен:");
            for(String cl:asm.symTab.keySet()){
                System.out.printf("%-12s %06X\n",
                    cl, asm.symTab.get(cl)
                );
            }
            System.out.println("_______________________");

            System.out.println("\tВторой проход:");
            if(asm.secondPass()){
                System.out.println("Обьектный код:");
                for(CodeLine cl:asm.codeLines){
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