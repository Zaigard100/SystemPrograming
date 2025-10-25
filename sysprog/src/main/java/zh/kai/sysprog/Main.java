package zh.kai.sysprog;

import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;

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
        //TODO Вывод реолизовать
        System.out.println("_______________________");

        System.out.println("Тадлица кодов:");
        System.out.println("_______________________");

        if(asm.firstPass()){
            for(CodeLine cl:asm.codeLines){
                System.out.println(cl.toAdditionString());
            }
            asm.secondPass();
        }

        System.out.println(Errors.getPart1());
        System.out.println(Errors.getPart2());
        System.out.println();
    }
}