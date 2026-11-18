package com.indra.jpa.inventory.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.service.ProductService;

@SpringBootTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @MockitoSpyBean
    private DataSource dataSource;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Busqueda de productos con timeout de consulta")
    void searchProductsAppliesQueryTimeout() throws Exception {
        List<Integer> timeouts = new ArrayList<>();
        doAnswer(invocation -> {
            Connection connection = (Connection) invocation.callRealMethod();
            return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class },
                    (proxy, method, arguments) -> {
                        try {
                            Object result = method.invoke(connection, arguments);
                            if (method.getName().equals("prepareStatement") && result instanceof PreparedStatement) {
                                PreparedStatement statement = (PreparedStatement) result;
                                return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                        new Class<?>[] { PreparedStatement.class }, (statementProxy, statementMethod,
                                                statementArguments) -> {
                                            try {
                                                if (statementMethod.getName().equals("executeQuery")) {
                                                    timeouts.add(statement.getQueryTimeout());
                                                }
                                                return statementMethod.invoke(statement, statementArguments);
                                            } catch (InvocationTargetException exception) {
                                                throw exception.getCause();
                                            }
                                        });
                            }
                            return result;
                        } catch (InvocationTargetException exception) {
                            throw exception.getCause();
                        }
                    });
        }).when(dataSource).getConnection();

        Page<Product> products = productRepository.searchProducts(null, PageRequest.of(0, 5));

        assertEquals(5, products.getContent().size());
        assertTrue(products.getTotalElements() > 5);
        assertEquals(List.of(5, 5), timeouts);
    }


    @Test
    @DisplayName("Search filters by minimum price")
    void searchProductsFiltersByMinPrice() {

        Page<Product> products = productService.searchProducts(null, new BigDecimal("100"),null,PageRequest.of(0, 50));

        assertFalse(products.isEmpty());

        assertTrue(products.getContent().stream()
                .allMatch(product -> product.getPrice().compareTo(new BigDecimal("100")) >= 0));
    }

    @Test
    @DisplayName("Search filters by maximum price")
    void searchProductsFiltersByMaxPrice() {

        Page<Product> products = productService.searchProducts(null,null,new BigDecimal("100"), PageRequest.of(0, 50));

        assertFalse(products.isEmpty());

        assertTrue(products.getContent().stream()
                .allMatch(product ->product.getPrice().compareTo(new BigDecimal("100")) <= 0));
    }

    @Test
    @DisplayName("Search filters by price range")
    void searchProductsFiltersByPriceRange() {

        BigDecimal minPrice = new BigDecimal("50");
        BigDecimal maxPrice = new BigDecimal("150");

        Page<Product> products = productService.searchProducts(null, minPrice, maxPrice,PageRequest.of(0, 50));

        assertFalse(products.isEmpty());

        assertTrue(products.getContent().stream()
                .allMatch(product -> product.getPrice().compareTo(minPrice) >= 0 && product.getPrice().compareTo(maxPrice) <= 0));
    }

    @Test
    @DisplayName("Search combines name and price range")
    void searchProductsFiltersByNameAndPriceRange() {

        BigDecimal minPrice = new BigDecimal("50");
        BigDecimal maxPrice = new BigDecimal("150");

        Page<Product> products = productService.searchProducts("keyboard",minPrice,maxPrice,PageRequest.of(0, 50));

        assertTrue(products.getContent().stream()
                .allMatch(product ->
                        product.getName().toLowerCase().contains("keyboard")
                        && product.getPrice().compareTo(minPrice) >= 0
                        && product.getPrice().compareTo(maxPrice) <= 0));
    }

}