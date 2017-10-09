package dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Coin implements Comparable<Coin>, Cloneable{
    private String name;
    private String currency;
    private Double price;
    private Double diff;

    @Override
    public int compareTo(Coin o) {
        if (this.getPrice() < o.getPrice())
            return -1;
        if (this.getPrice().equals(o.getPrice()))
            return 0;

        return 1;
    }

    @Override
    public Coin clone() throws CloneNotSupportedException {
        return (Coin)super.clone();
    }
}
