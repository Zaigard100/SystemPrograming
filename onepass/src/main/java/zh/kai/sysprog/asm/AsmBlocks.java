package zh.kai.sysprog.asm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    Map<Address,String> relocationTable;

    Map<String,Address> externalLinks;
    List<String> externalSymbols;

    CodeLine header;

    int lc;

    public AsmBlocks(AddressationType type) {
        this.type = type;
    }

    public void init(){
        codeLines = new ArrayList<>();
        metLabels = new HashMap<>();
        symTab = new HashMap<>();
        relocationTable = new HashMap<>();
        externalLinks = new HashMap<>();
        externalSymbols = new ArrayList<>();
        lc = -1;
    }   

    public boolean addError(String string, CodeLine codeLine){return false;};

    public Optional<Operation> getOperation(String opName){
        for(Operation o: operationsTable){
            if(o.getOperationName().equals(opName)) return Optional.of(o);
        }
        return Optional.empty();
    }
}
