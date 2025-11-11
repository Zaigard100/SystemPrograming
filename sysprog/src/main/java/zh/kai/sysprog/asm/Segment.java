package zh.kai.sysprog.asm;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

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



}
