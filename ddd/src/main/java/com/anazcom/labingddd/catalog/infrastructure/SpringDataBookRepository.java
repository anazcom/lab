package com.anazcom.labingddd.catalog.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataBookRepository extends JpaRepository<BookJpaEntity, String> {
}
