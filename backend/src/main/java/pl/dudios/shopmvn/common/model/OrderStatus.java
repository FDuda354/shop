package pl.dudios.shopmvn.common.model;

public enum OrderStatus {
    NEW("NEW"),
    PAID("PAID"),
    PROCESSING("PROCESSING"),
    WAITING_FOR_DELIVERY("WAITING_FOR_DELIVERY"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    REFUND("REFUND");

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
