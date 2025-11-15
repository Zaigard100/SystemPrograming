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

    

}
