package com.indra.jpa.inventory.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.indra.jpa.inventory.model.Product;

@SpringBootTest
class ProductRepositoryTest {

    private final ProductRepository productRepository;

    @MockitoSpyBean
    private DataSource dataSource;

    @Autowired
    ProductRepositoryTest(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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
    @DisplayName("Bonus: Búsqueda con filtros de precio en JPQL retorna productos en el rango")
    void searchProductsWithPriceRangeFiltersCorrectly() {
        Page<Product> products = productRepository.searchProductsWithPriceRange(
                "Mouse",
                new BigDecimal("48.00"),
                new BigDecimal("50.00"),
                PageRequest.of(0, 10)
        );

        assertNotNull(products);
        assertTrue(products.getTotalElements() > 0);
        assertTrue(products.getContent().stream()
                .allMatch(product -> product.getPrice().compareTo(new BigDecimal("48.00")) >= 0
                        && product.getPrice().compareTo(new BigDecimal("50.00")) <= 0
                        && product.getName().toLowerCase().contains("mouse")));
    }

    @Test
    @DisplayName("Bonus: Búsqueda dinámica con Specification y JpaSpecificationExecutor")
    void searchWithSpecificationFiltersCorrectly() {
        Specification<Product> specification = ProductSpecification.withFilters(
                "Keyboard",
                new BigDecimal("120.00"),
                new BigDecimal("130.00")
        );

        Page<Product> products = productRepository.findAll(specification, PageRequest.of(0, 10));

        assertNotNull(products);
        assertTrue(products.getTotalElements() > 0);
        assertTrue(products.getContent().stream()
                .allMatch(product -> product.getPrice().compareTo(new BigDecimal("120.00")) >= 0
                        && product.getPrice().compareTo(new BigDecimal("130.00")) <= 0
                        && product.getName().toLowerCase().contains("keyboard")));
    }

}
