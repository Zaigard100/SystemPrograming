package zh.kai.sysprog;

import zh.kai.sysprog.asm.Errors;

public class Main {

    static final String text = """
    .prog start 100
        ldar1 .one
        mov r1 r2
        ldar1 .two
        add r1 r2
        star1 .res
        int 20
        clr
        resb 4
    .data
    .bighex byte X"0FAF9"
        resw 5
    .one byte 1
    .two byte 2
    .res word 34
    .hello byte C"Hello"
        end
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
        System.out.println("Hello world!");

        Assembler asm = new Assembler();
        asm.firstPass();
        System.out.println(Errors.getPart1());
    }
}