package zh.kai.sysprog.asm;

import java.util.List;

public class Segment extends AsmBlocks{
    Assembler asm;

    public Segment(AddressationType type,List<Operation> operationsTable,Assembler asm) {
        super(type);
        this.operationsTable = operationsTable;
        this.asm = asm;
    }

    @Override
    public boolean addError(String err,CodeLine cl){
        return asm.addError(err,cl);
    }


}
