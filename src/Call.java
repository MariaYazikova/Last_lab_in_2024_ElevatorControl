import java.util.Objects;

//класс вызова
public class Call {
    private final int floor; //этаж

    public Call(int floor) {
        this.floor = floor;
    }

    public Call(){
        this.floor = 0;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Call call = (Call) obj;
        return floor == call.floor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("этаж=").append(floor);
        return str.toString();
    }
}