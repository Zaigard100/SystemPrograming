package zh.kai.sysprog.asm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.text.Utilities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeLine {

    boolean isPassed = false;

    private String label;
    private String operationName;
    private String argument;

    private Address address;
    private short[] objectCode;

    public CodeLine(String label, String operationName, String argument) {
        this.label = label;
        this.operationName = operationName;
        this.argument = argument;
    }

    public boolean eqName(String n){
        return n.equals(operationName);
    }

    public boolean pass(AsmBlocks asm){
        int lc = asm.getLc();
        if (operationName !=null) {
            if(Assembler.dirrectives.contains(operationName)){
                switch (operationName) {
                    case "start" -> {
                        if(label == null){
                            return asm.addError("Ожидется что у "+operationName+" будет имя",this);
                        }
                        if(argument == null){
                            return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                        }
                        asm.setBlockName(label);
                        
                        lc = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                        
                        if(lc!=0){ 
                            return asm.addError("Не допустимый адрес", this);
                        }
                        asm.setLc(lc);
                        address = new Address(lc);
                        short[] binAddress = Utils.intToBin(lc);
                        objectCode = new short[]{binAddress[0],binAddress[1],binAddress[2],0xFF,0xFF,0xFF};
                        asm.setHeader(this);
                    }
                    case "end" -> {
                        int start = asm.getHeader().getAddress().getAddress();
                        if(argument==null){
                            objectCode = asm.getHeader().getAddress().toBin().clone();
                            return true;
                        }
                        int arg = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                        if(arg<start && arg>lc){
                            return asm.addError("Не допустимый аргумент", this);
                        }
                        short[] adr = Utils.intToBin(arg);

                        int diff = lc - start;
                        short[] len = Utils.intToBin(diff);

                        short[] h = asm.getHeader().getObjectCode();
                        h[3] = len[0];
                        h[4] = len[1];
                        h[5] = len[2];

                        asm.getHeader().setObjectCode(h);

                        objectCode = new short[]{adr[0],adr[1],adr[2]};

                        if(!asm.getMetLabels().isEmpty()){
                            asm.addError("Встреченные имена небыли найдены полностью", this);
                        }
                    }
                    case "resb" -> {
                        address = new Address(lc);

                        if(argument == null){
                            return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                        }
                        int len = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                        if(len == -1) return asm.addError("Ожидется что аргумент число",this);
                        objectCode = new short[len];

                        asm.setLc(lc+len);
                    }
                    case "resw" -> {
                        address = new Address(lc);

                        if(argument == null){
                            return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                        }
                        int len = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                        if(len == -1) return asm.addError("Ожидется что аргумент число",this);
                        objectCode = new short[len*Utils.WORD_LENGHT];

                        asm.setLc(lc+(len*Utils.WORD_LENGHT));
                    }
                    case "word" -> {
                        address = new Address(lc);

                        if(argument == null){
                            return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                        }
                        int data = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                        if(data == -1) return asm.addError("Ожидется что аргумент число",this);
                        if(data>=0 && data>Utils.WORD_MAX) return asm.addError("Не корректные данные",this);
                        objectCode = Utils.intToBin(data);

                        asm.setLc(lc+Utils.WORD_LENGHT);
                    }
                    case "byte" -> {
                        address = new Address(lc);

                        if(argument == null){
                            return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                        }

                        if(argument.startsWith("C")){
                            String arg = argument.substring(2, argument.length()-1);
                            objectCode = Utils.stringToAsciiShortArray(arg);
                        }else if(argument.startsWith("X")){
                            String arg = argument.substring(2, argument.length()-1);
                            objectCode = Utils.hexStringToShortArray(arg);
                        }else{
                            int data = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                            if(data == -1) return asm.addError("Ожидется что аргумент число",this);
                            if(data>=0 && data>Utils.MAX_BYTE) return asm.addError("Не корректные данные",this);
                            objectCode = new short[]{Utils.intToBin(data)[2]};
                        }

                        asm.setLc(lc + objectCode.length);
                    }
                }
            }else{
                Optional<Operation> op = asm.getOperation(operationName);
                if(op.isPresent()){
                    Operation currentOperation = op.get();
                    int len = currentOperation.getLenght();
                    short code = currentOperation.getCode();
                    code *= 4;
                    address = new Address(lc);
                    switch (len) {
                        case 1 -> {
                            if(argument!=null){
                                return asm.addError(operationName+" не дожен иметь аргумент",this);
                            }   objectCode = new short[]{code};
                        }
                        case 2 -> {
                            if(argument==null){
                                return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                            }   
                            String[] split = argument.split("\\s+");
                            if(split.length==2){
                                if(split[0].matches("^r(0|[1-9]|1[0-5])$") && split[1].matches("^r(0|[1-9]|1[0-5])$")){
                                    split[0] = split[0].substring(1);
                                    split[1] = split[1].substring(1);
                                    short reg = (short) Utils.parseAndValidateArgument(split[0], asm, this).orElse(-1);
                                    reg = (short) (reg << 4);
                                    reg += (short) Utils.parseAndValidateArgument(split[1], asm, this).orElse(-1);
                                    objectCode = new short[]{code,reg};
                                }else{
                                    return asm.addError("Ожидется регистр",this);
                                }
                            }else if (split.length == 1) {
                                int data = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                                if(data == -1) return asm.addError("Ожидется что аргумент число",this);
                                if(data>=0 && data>Utils.MAX_BYTE) return asm.addError("Не корректные данные",this);
                                objectCode = new short[]{(short) (code+1),Utils.intToBin(data)[2]};
                            }
                        }
                        case 4 -> {
                            if(argument==null){
                                return asm.addError("Ожидется что у "+operationName+" будет аргумент",this);
                            }
                            if(argument.startsWith("[")){
                                if(asm.getType().equals(AddressationType.DIRECT)){
                                    asm.addError("Прямая адресация недоступна", this);
                                    return false;
                                }
                                if(argument.endsWith("]")){
                                    String arg = argument.trim().substring(1, argument.length()-1);
                                    if(arg.startsWith(".")){
                                        if(asm.getSymTab().keySet().contains(arg)){
                                            Address adr = asm.getSymTab().get(arg);
                                            int diff = adr.getAddress() - (address.getAddress() + len);
                                            short[] a = Utils.intToBin(diff);
                                            objectCode = new short[]{(short) (code+1),a[0],a[1],a[2]};
                                        }else{
                                            objectCode = new short[]{(short) (code+1),0xff,0xff,0xff};
                                            asm.getMetLabels().put(this, arg);
                                        }
                                    }else{        
                                        int data = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                                        if(data == -1) return asm.addError("Ожидется что аргумент число",this);
                                        if(data>=0 && data>Utils.MAX_BYTE) return asm.addError("Не корректные данные",this);
                                        int diff = address.getAddress() + len;
                                        short[] a = Utils.intToBin(diff);
                                        objectCode = new short[]{(short) (code+1),a[0],a[1],a[2]};
                                    }
                                }else{
                                    asm.addError("Ожидается ]", this);
                                }
                            }else if(argument.startsWith(".")){
                                if(asm.getType().equals(AddressationType.RELATIVE)){
                                    asm.addError("Прямая адресация недоступна", this);
                                    return false;
                                }
                                if(asm.getSymTab().keySet().contains(argument)){
                                    Address adr = asm.getSymTab().get(argument);
                                    short[] a = adr.toBin();
                                    objectCode = new short[]{(short) (code+1),a[0],a[1],a[2]};
                                }else{
                                    objectCode = new short[]{(short) (code+1),0xff,0xff,0xff};
                                    asm.getMetLabels().put(this, argument);
                                }
                                asm.relocationTable.add(address);
                            }else{
                                int data = Utils.parseAndValidateArgument(argument, asm, this).orElse(-1);
                                if(data == -1) return asm.addError("Ожидется что аргумент число",this);
                                if(data>=0 && data>Utils.MAX_BYTE) return asm.addError("Не корректные данные",this);
                                short[] bin = Utils.intToBin(data);
                                objectCode = new short[]{(short) (code+1),bin[0],bin[1],bin[2]};
                            }
                        }
                        default -> {
                            return asm.addError("Не корректная длина",this);
                        }
                    }
                    asm.setLc(lc+objectCode.length);
                }else{
                    return asm.addError("Команда "+operationName+" не найдена", this);
                }
            }
        }else{
            address = new Address(lc);
        }

        if(label!=null) {
            if(!isHead()) asm.getSymTab().put(label, address);
            ArrayList<CodeLine> toRemove = new ArrayList<>();
            for(CodeLine cl:asm.getMetLabels().keySet()){
               
                if(label.equals(asm.getMetLabels().get(cl))){
                    if(cl.getArgument().startsWith("[")){
                        int diff = address.getAddress() - (cl.getAddress().getAddress() + cl.getObjectCode().length);
                        short[] a = Utils.intToBin(diff);
                        cl.getObjectCode()[1] = a[0];
                        cl.getObjectCode()[2] = a[1];
                        cl.getObjectCode()[3] = a[2];
                        toRemove.add(cl);
                    }else{
                        cl.getObjectCode()[1] = address.toBin()[0];
                        cl.getObjectCode()[2] = address.toBin()[1];
                        cl.getObjectCode()[3] = address.toBin()[2];
                        toRemove.add(cl);
                    }
                }
            }
            for (CodeLine elem : toRemove) {
                asm.getMetLabels().remove(elem);
            }
        }
        isPassed = true;
        return true;
    }

    public boolean isHead(){
        return "start".equals(operationName);
    }
    public boolean isEnd(){
        return "end".equals(operationName);
    }
    public boolean isLabelLine(){
        return label!=null && operationName==null;
    }

    @Override
    public String toString() {
        String l = "";
        String o = "";
        String a = "";
        if(label!=null) l = label;
        if(operationName!=null) o = operationName;
        if(argument!=null) a = argument;
        return String.format("%-8s %s %s", l, o, a);
    }

    public String toBin(){
        if(isHead()){
            return "H "+label+" "+Utils.byteArrrayToString(objectCode);
        }
        if(isEnd()){
            return "E "+ Utils.byteArrrayToString(objectCode);
        }
        if(isLabelLine()){
            return "";
        }
        return "T " +address.toString()+" "+objectCode.length+" "+Utils.byteArrrayToString(objectCode);
        
    }
    

}
