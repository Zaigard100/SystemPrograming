package zh.kai.sysprog.asm;

public class Errors {
    static private String part1 = "";
    static private String part2 = "";

    public static void  addPart1(String err){
        part1 += err + "\n";
    }

    public static void  addPart2(String err){
        part2 += err + "\n";
    }

    public static String getPart1() {
        return part1;
    }


    public static String getPart2() {
        return part2;
    }



}
