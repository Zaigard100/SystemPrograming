package zh.kai.sysprog.asm;

public class Operation {
    private String name;
    private short code; // для храненеия безнакового byte используем short
    private int lenght;

    public Operation(String name,short code, int lenght){
        this.name = name;
        this.code = code;
        this.lenght = lenght;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    

}
