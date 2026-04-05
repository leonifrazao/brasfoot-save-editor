package br.com.saveeditor.brasfoot.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.saveeditor.brasfoot")
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_application_or_adapters = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapters..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapters..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule in_ports_should_be_named_use_case = classes()
            .that().resideInAPackage("..application.ports.in..").and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule out_ports_should_be_named_port = classes()
            .that().resideInAPackage("..application.ports.out..").and().areInterfaces()
            .should().haveSimpleNameEndingWith("Port")
            .allowEmptyShould(true);
}