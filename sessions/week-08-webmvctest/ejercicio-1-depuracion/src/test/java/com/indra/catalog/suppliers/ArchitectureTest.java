package com.indra.catalog.suppliers;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/** Bonus: evita que `web` vuelva a acoplarse a una implementación concreta del servicio. */
class ArchitectureTest {

    @Test
    void webNoDependeDeImplementacionesConcretasDelServicio() {
        var classes = new ClassFileImporter().importPackages("com.indra.catalog.suppliers");

        ArchRule rule = noClasses().that().resideInAPackage("..web..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("ServiceImpl");

        rule.check(classes);
    }
}
