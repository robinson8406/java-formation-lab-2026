package co.com.inventory.inventoryservice.usecases;

import co.com.inventory.inventoryservice.models.ProductDto;

import java.util.List;
import java.util.Map;

public interface IInventoryGetUseCase {

    public List<ProductDto> read(Map<String, String> pathVariables) ;



}
