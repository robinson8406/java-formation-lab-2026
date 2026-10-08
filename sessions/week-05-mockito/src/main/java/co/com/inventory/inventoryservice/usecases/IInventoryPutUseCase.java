package co.com.inventory.inventoryservice.usecases;

import co.com.inventory.inventoryservice.models.ProductDto;

public interface IInventoryPutUseCase {

    public void update(ProductDto productDto) throws IllegalStateException, IllegalArgumentException ;
    
}
