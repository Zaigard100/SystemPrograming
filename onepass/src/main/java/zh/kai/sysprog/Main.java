package zh.kai.sysprog;


import zh.kai.sysprog.asm.Assembler;

public class Main {

    public final static String CODE = 
    """
    .prog start
    extref .key .hash
    extdef .hex .res
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

    .s1 segment
    extref .hex
    extdef .key
        lda .hex
        mov r1 r12
        lda .key
        add r1 r12
        sta .key
    .key word 42
    end .s1

    .s2 segment
    extref .res
    extdef .hash
        clr
        sta .res
        lda .hash
        mov r1 r7
        mov 24
        sub r1 r7
        sta .res
    .hash byte X"EAFE6B6D99"  
    end .s2

    end .prog
    """;

    public final static String OPER = 
    """
    mov 1 2
    lda 2 4
    sta 3 4
    add 4 2
    sub 6 2
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