package br.com.saveeditor.brasfoot.architecture;

import br.com.saveeditor.brasfoot.application.services.ManagerManagementService;
import br.com.saveeditor.brasfoot.application.services.PlayerManagementService;
import br.com.saveeditor.brasfoot.application.services.TeamManagementService;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.saveeditor.brasfoot", importOptions = ImportOption.DoNotIncludeTests.class)
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
    static final ArchRule application_must_not_depend_on_service_package = noClasses()
            .that().resideInAPackage("..application..")
            .and().doNotHaveFullyQualifiedName(TeamManagementService.class.getName())
            .and().doNotHaveFullyQualifiedName(PlayerManagementService.class.getName())
            .and().doNotHaveFullyQualifiedName(ManagerManagementService.class.getName())
            .should().dependOnClassesThat().resideInAPackage("..service..")
            .because("application layer must consume ports instead of concrete services; temporary exceptions are tracked in phase plans")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_must_not_depend_on_model_package = noClasses()
            .that().resideInAPackage("..domain..")
            .and().doNotHaveFullyQualifiedName(SaveContext.class.getName())
            .should().dependOnClassesThat().resideInAPackage("..model..")
            .because("domain models should stay detached from legacy model package during migration")
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
