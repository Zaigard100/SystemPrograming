package zh.kai.sysprog.utils;

public class Pair<T,P> {
    private T el1;
    private P el2;

    public Pair(T el1,P el2){
        this.el1 =el1;
        this.el2 =el2;
    }

    public T getEl1() {
        return el1;
    }

    public void setEl1(T el1) {
        this.el1 = el1;
    }

    public P getEl2() {
        return el2;
    }

    public void setEl2(P el2) {
        this.el2 = el2;
    }



}
