package com.anazcom.labingddd.shared;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureTests {

  @Test
  void domainCannotHaveBootPackages() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.anazcom.labingddd");

    ArchRule rule =
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..", "jakarta.persistence..", "javax.persistence..");

    rule.check(importedClasses);
  }

  @Test
  void infrastructureClassesShouldBePackagePrivate() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.anazcom.labingddd");

    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage("..infrastructure..")
            .should()
            .bePackagePrivate();

    rule.check(importedClasses);
  }
}
