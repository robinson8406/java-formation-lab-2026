package co.com.inventory.inventoryservice.usecases;

import co.com.inventory.inventoryservice.models.ProductDto;

public interface IInventoryPatchUseCase {

    public void update(String id, ProductDto productDto) throws IllegalStateException, IllegalArgumentException ;

    
}
