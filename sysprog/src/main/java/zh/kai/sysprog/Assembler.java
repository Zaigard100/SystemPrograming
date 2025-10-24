package zh.kai.sysprog;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Operation;
import zh.kai.sysprog.utils.Pair;

public class Assembler {

    ArrayList<Operation> operationsCodes;
    CodeLine header;
    ArrayList<CodeLine> codeLines;
    
    public Assembler(){

    }
    
    public Assembler(String code,String operationCode){
        
    }

    public ArrayList<Operation> parseOperationsCodes(String text){
        ArrayList<Operation> opCod = new ArrayList<>();

        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNext()) {
                String line = sc.nextLine().trim();
                String[] split = line.split(" ", 3);

                String name = split[0];
                short c = Short.parseShort(split[1]);
                int lenght = Integer.parseInt(split[2]);
                
                opCod.add(new Operation(name, c, lenght));
            }
        }

        return opCod;
    }

    public ArrayList<CodeLine> parseCode(String text){
        
        ArrayList<CodeLine> code = new ArrayList<>();

        try (Scanner sc = new Scanner(text)) {
            while (sc.hasNext()) {
               String line = sc.nextLine().trim();
               String[] split;
               if(line.startsWith(".")){
                    split = line.split(" ",3);
                    switch (split.length) {
                       case 3 -> code.add(new CodeLine(split[0], split[1],split[2]));
                       case 2 -> code.add(new CodeLine(split[0], split[1], null)); //без аргументов
                       case 1 -> code.add(new CodeLine(split[0], null, null));//строка метка
                   }
               }else{
                    split = line.split(" ",2);
                   switch (split.length) {
                       case 2 -> code.add(new CodeLine(null, split[0],split[1]));
                       case 1 -> code.add(new CodeLine(null, split[0], null)); //без аргументов
                   }
               }
            }
        }

        return code;
    }
}
