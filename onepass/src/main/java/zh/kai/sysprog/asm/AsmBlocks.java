package zh.kai.sysprog.asm;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsmBlocks {

    String blockName;

    List<CodeLine> codeLines;
    List<Operation> operationsTable;

    Map<CodeLine,String> metLabels;
    Map<String,Address> symTab;

    CodeLine header;

    int lc;

    public boolean addError(String string, CodeLine codeLine){return false;};

    public Optional<Operation> getOperation(String opName){
        for(Operation o: operationsTable){
            if(o.getOperationName().equals(opName)) return Optional.of(o);
        }
        return Optional.empty();
    }
}
