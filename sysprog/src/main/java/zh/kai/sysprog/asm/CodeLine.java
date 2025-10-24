package zh.kai.sysprog.asm;

public class CodeLine {

    private String label; //метка
    private String operationName;
    private String arguments;

    private int address;
    private int lenght;
    private byte[] obj;

    public CodeLine(String label, String operationName,String arguments) {
        this.arguments = arguments;
        this.label = label;
        this.operationName = operationName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(label!=null) sb.append(label).append(" ");
        if(operationName!=null) sb.append(operationName).append(" ");
        if(arguments!=null) sb.append(arguments).append(" ");
        return sb.toString();
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    public byte[] getObj() {
        return obj;
    }

    public void setObj(byte[] obj) {
        this.obj = obj;
    }

}
