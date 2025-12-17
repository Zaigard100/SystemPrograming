package zh.kai.sysprog.asm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    private boolean isEmpty = false;
    public Address empty(){
        isEmpty = true;
        return this;
    }
    static final Address EMPTY = new Address(0xffffff).empty();

    int address; 

    public Address(int address) {
        this.address = address;
    }

    public short[] toBin(){
        short[] bin = Utils.intToBin(address);
        return new short[]{bin[0],bin[1],bin[2]};
    }

    @Override
    public String toString() {
        return Utils.byteArrrayToString(toBin());
    }

    

}
