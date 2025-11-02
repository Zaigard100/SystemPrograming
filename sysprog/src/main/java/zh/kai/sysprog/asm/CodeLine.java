package zh.kai.sysprog.asm;

import zh.kai.sysprog.Assembler;
import zh.kai.sysprog.utils.Utils;

public class CodeLine {

    private String label; //метка
    private String operationName;
    private String arguments;

    private Address address;
    private int lenght;
    private short [] obj;

    public CodeLine(String label, String operationName,String arguments) {
        this.arguments = arguments;
        this.label = label;
        this.operationName = operationName;
    }

    public boolean checkLenght(){

        float len = 1;

        if(arguments!=null){
            if(Assembler.dirrectives.contains(operationName)){
                return true; 
                // сомнительно но ОК!! (т.к. длину мы вычисляем из функции 
                // dirrectiveLenght(CodeLine currentLine)) то предпологаем 
                // что длина верна.
            }

            String[] args = arguments.split(" ");

            for (String a : args) {
                if(a.startsWith(".")){
                    if(lenght==4){
                        len+=Assembler.WORD_LENGHT;
                    }else if(lenght == 3){
                        len+=Assembler.REL_LENGHT;
                    }
                }else if(a.startsWith("r")){
                    len += 0.5;
                }else if(Utils.isIntegerRegex(a)){
                    if(Integer.parseInt(a)>=0){
                        if(lenght == 2){
                            if(Integer.parseInt(a)<Assembler.MAX_BYTE){
                                return true;
                            }
                        }
                        if(lenght == 3){
                            if(Integer.parseInt(a)<Assembler.REL_MAX){
                                return true;
                            }
                        }
                        if(lenght == 4){
                            if(Integer.parseInt(a)<Assembler.WORD_MAX){
                                return true;
                            }
                        }
                    }
                    Errors.addPart1("Неверный аргумент: " + toString());
                    return false;
                }else{
                    Errors.addPart1("Неверный аргумент: " + toString());
                    return false;
                }
            }
        }
        if(Math.abs(lenght-len)<=0.0001f){
            return true;
        }
        Errors.addPart1("Неверная длина команды: " + toString());
        return false;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(label!=null) sb.append(label).append(" ");
        if(operationName!=null) sb.append(operationName).append(" ");
        if(arguments!=null) sb.append(arguments).append(" ");
        return sb.toString();
    }

    public String toAdditionString(){
        StringBuilder sb = new StringBuilder();
        if(operationName!=null) sb.append(operationName).append(" ");
        if(arguments!=null) sb.append(arguments).append(" ");
        if(address == null){
            return String.format(sb.toString());
        }
        return String.format("%06X %s", address.getAddress(),sb.toString());
    }

    public String toObjString(){
        StringBuilder sb = new StringBuilder();
        switch (operationName) {
            case "start" -> {
                sb.append("H ").append(label).append(" ");
                int i = 0;
                for(short b:obj){
                    sb.append(String.format("%02x", b));
                    if(i==3) sb.append(" ");
                    i++;
                }
            }
            case "end" -> {
                sb.append("E ");
                for(short b:obj){
                    sb.append(String.format("%02x", b));
                }
            }
            default -> {
                sb.append("T ");
                short[] addr = address.toBytes();
                for(short b:addr){
                    sb.append(String.format("%02x", b));
                }   sb.append(" ");
                addr = Utils.intToShortArray4(lenght);
                sb.append(String.format("%02x", addr[3]));
                sb.append(" ");
                if(!(operationName.equals("resb") || operationName.equals("resw") )){
                    for(short b:obj){
                        sb.append(String.format("%02x", b));
                    }
                }
            }
        }
        return sb.toString();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    public short[] getObj() {
        return obj;
    }

    public void setObj(short[] obj) {
        this.obj = obj;
    }

}
