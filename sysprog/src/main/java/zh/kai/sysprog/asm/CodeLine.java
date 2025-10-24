package zh.kai.sysprog.asm;

public class CodeLine {

    private String label; //метка
    private String operationName;
    private String arguments;

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

    

}
