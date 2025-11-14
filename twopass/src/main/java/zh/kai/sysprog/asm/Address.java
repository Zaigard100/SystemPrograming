package zh.kai.sysprog.asm;

import zh.kai.sysprog.utils.Utils;

public class Address {

    private int address; 

    public Address(int addr){
        address = addr;
    }

    public Address(String num){
        try{
            address = Integer.parseInt(num);
        }catch(NumberFormatException e){
            throw new RuntimeException("NumberFormatException");
        }
    }

    public short[] toBytes(){
        short[] b = Utils.intToShortArray4(address);
        return new short[]{b[1],b[2],b[3]};
    }

    public int getAddress(){
        return address;
    }

    public void setAddress(int addr){
        address = addr;
    }

    


}
