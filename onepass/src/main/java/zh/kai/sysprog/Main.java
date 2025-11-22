package zh.kai.sysprog;


import zh.kai.sysprog.asm.Assembler;

public class Main {

    public final static String CODE = 
    """
    .prog start 0
    .loop
        lda .one
        mov r1 r2
        lda [.two]
        mov r1 r3
        add r1 r2
    .save sta [.res]
        clr
        jmp .loop 
    .one byte 1
    .two byte 2
    .res word 0
    .hex byte X"ABCDEF45"
    .hello byte C"Hello"
    end 101
    """;

    public final static String OPER = 
    """
    mov 1 2
    lda 2 4
    sta 3 4
    add 4 2
    jmp 8 4
    clr 5 1
    """;

    public static void main(String[] args) {
        Assembler asm = new Assembler(CODE, OPER);
        asm.init();
        System.out.println(asm.getSourseCode());
        for(String s:asm.getErrors()){
            System.out.println(s+"\n");
        }
        System.out.println("");

        asm.passFull();

        System.out.println();

        System.out.println(asm.toBin());

    }
}