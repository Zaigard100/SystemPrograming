package zh.kai.sysprog.asm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {

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
