# Enunciado — Week 05: Mockito (Buenas Prácticas)

## Contexto del reto

Completar los casos de prueba utilizando Mockito para la clase InventoryService.


## Lo que debes implementar

1. Implementa la libreria de Mockito sobre la clase `InventoryServiceTest`. TIP: Recuerda que inyectamos la libreria con `@ExtendWith(MockitoExtension.class)`.
2. Mockea el atributo `ICatalogRepository`. TIP: Recuerda que el objeto a Mock lo indicamos con @Mock 
3. Indica cual es la clase a testear utilizando mock. TIP: Recuerda que la clase a testear la indicamos con @InjectMocks. 
4. Completa los casos de prueba:

   4.1. En el caso de prueba de creación adiciona el condicional WHEN

   4.2. En el caso de prueba de actualización adiciona el verify.

   4.3. En el caso de prueba de consultar por ID agregar el WHEN 


## Criterio de aceptación del PR

- [ ] Los casos de pruebas son ejecutados exitosamente


