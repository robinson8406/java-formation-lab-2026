package co.com.inventory.inventoryservice.usecases;

import co.com.inventory.inventoryservice.models.ProductDto;

public interface IInventoryPostUseCase {

    public String create(ProductDto productDto) throws IllegalStateException, IllegalArgumentException ;


}
