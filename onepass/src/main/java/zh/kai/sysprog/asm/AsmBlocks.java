package zh.kai.sysprog.asm;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsmBlocks {

    AddressationType type;

    String blockName;

    List<CodeLine> codeLines;
    List<Operation> operationsTable;

    Map<CodeLine,String> metLabels;
    Map<String,Address> symTab;
    List<Address> relocationTable;

    CodeLine header;

    int lc;

    public AsmBlocks(AddressationType type) {
        this.type = type;
    }

    

    public boolean addError(String string, CodeLine codeLine){return false;};

    public Optional<Operation> getOperation(String opName){
        for(Operation o: operationsTable){
            if(o.getOperationName().equals(opName)) return Optional.of(o);
        }
        return Optional.empty();
    }
}
