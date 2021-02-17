package tesi.smartwasting;

public enum Category {
    CARTA(0),
    PLASTICA(1),
    VETRO(2);
    private final int id;
    Category(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public final static Category getEnum(int id) {
        switch (id) {
            case 0: return Category.CARTA;
            case 1: return Category.PLASTICA;
            case 2: return Category.VETRO;
            default: return null;
        }
    }
}