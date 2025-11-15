package zh.kai.sysprog.asm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Operation {

    private String operationName;
    private short  code;
    private int lenght;

    public Operation(String operationName, short code, int lenght) {
        this.operationName = operationName;
        this.code = code;
        this.lenght = lenght;
    }

    public boolean eq(String t){
        return t.equals(operationName);
    }

}
