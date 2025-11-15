package zh.kai.sysprog.asm;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import zh.kai.sysprog.Assembler;
import zh.kai.sysprog.Pass;

@Getter
@Setter
public class Segment {

    private String name;

    private ArrayList<CodeLine> codeLines;
    private HashMap<String,Address> symTab;
    private HashMap<Address,String> relocationTable;
    private HashMap<String,Address> externalLinks;
    private ArrayList<String> externalSymbols;

    public Segment(String name){
        this.name = name;
        codeLines = new ArrayList<>();
        symTab = new HashMap<>();
        relocationTable = new HashMap<>();
        externalLinks = new HashMap<>();
        externalSymbols = new ArrayList<>();
    }

    public boolean firstPass(Assembler asm){
        CodeLine header = codeLines.getFirst();
        if(!header.getOperationName().equals("segment")){
            Errors.addPart1("Не найдено начало сегмента");
            return  false;
        }
        return Pass.first(
            asm.getOperationsCodes(),
            0,
            false,
            header,
            codeLines,
            symTab,
            externalLinks,
            externalSymbols,
            asm.getType());
    }

    public boolean secondPass(Assembler asm){
        CodeLine header = codeLines.getFirst();
        return Pass.second(
            asm.getOperationsCodes(), 
            false, 
            header, 
            codeLines, 
            symTab, 
            externalLinks, 
            externalSymbols, 
            relocationTable, 
            asm.getType()
        );
    }


    public String getAuxiliaryTable(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl:codeLines){
            sb.append(cl.toAdditionString()).append("\n");
        }
        return  sb.toString();
    }
    public String getObjText(){
        StringBuilder sb = new StringBuilder();
        for(CodeLine cl:getCodeLines()){
            if(cl.isSegment()){
                sb.append(cl.toObjString()).append("\n");
            }else if (cl.isExtdef()) {
                String[] defs = cl.getArguments().trim().split(" ");
                for(String s:defs){
                    sb.append(String.format("D %s %06X \n",s,symTab.get(s).getAddress()));
                }
            }else if (cl.isExtref()) {
                String[] defs = cl.getArguments().trim().split(" ");
                for(String s:defs){
                    sb.append("R ").append(s).append("\n");
                }
            }else if(cl.isEnd()){
                for(Address adr: relocationTable.keySet()){
                    String format = String.format("M %06X %s", adr.getAddress(),relocationTable.get(adr));
                    sb.append(format).append("\n");
                }
                sb.append(cl.toObjString()).append("\n");
            }else{
                sb.append(cl.toObjString()).append("\n");
            }
        }
        return sb.toString().trim();
    }

}
