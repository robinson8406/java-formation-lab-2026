package co.com.inventory.inventoryservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "Product")

public class Product {

    @Id
    private String id ;

    @Column(nullable = false, unique = true)
    private String name ;

    @Column
    private String description ;

    @Column
    private String units ;

    @Column
    private Double quantity ;

}
