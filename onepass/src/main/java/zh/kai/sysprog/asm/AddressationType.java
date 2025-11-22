package zh.kai.sysprog.asm;

public enum AddressationType {
    CHAINED("Смешанная"),
    DIRECT("Прямая"),
    RELATIVE("Относительная");

    String name;

    private AddressationType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
