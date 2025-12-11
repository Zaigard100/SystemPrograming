package zh.kai.sysprog.asm;

import java.util.List;

public class Segment extends AsmBlocks{

    public Segment(AddressationType type,List<Operation> operationsTable) {
        super(type);
        this.operationsTable = operationsTable;
        
    }

}
