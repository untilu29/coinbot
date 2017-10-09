package dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Coin implements Comparable<Coin>{
    private String name;
    private String currency;
    private Double price;
    private Double diff;

    @Override
    public int compareTo(Coin o) {
        if (o.getPrice()==null) return 1;
        if (this.getPrice() < o.getPrice())
            return -1;
        if (this.getPrice().equals(o.getPrice()))
            return 0;

        return 1;
    }
}
