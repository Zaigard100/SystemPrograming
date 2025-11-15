package zh.kai.sysprog.asm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeLine {

    private String label;
    private String operationName;
    private String argument;

    private short[] objectCode;

    public CodeLine(String label, String operationName, String argument) {
        this.label = label;
        this.operationName = operationName;
        this.argument = argument;
    }

    public boolean eqName(String n){
        return n.equals(operationName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(label!=null) sb.append(label).append(" ");
        if(operationName!=null) sb.append(operationName).append(" ");
        if(argument!=null) sb.append(argument).append(" ");
        return sb.toString();
    }

    

}
