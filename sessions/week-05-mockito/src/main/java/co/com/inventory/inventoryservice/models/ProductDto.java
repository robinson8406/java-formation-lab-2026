package co.com.inventory.inventoryservice.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class ProductDto {

    private String id ;

    private String name ;

    private String description ;

    private String units ;

    private Double quantity ;


}
