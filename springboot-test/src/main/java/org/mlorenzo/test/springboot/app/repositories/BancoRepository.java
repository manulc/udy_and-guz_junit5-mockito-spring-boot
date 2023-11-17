package org.mlorenzo.test.springboot.app.repositories;

import org.mlorenzo.test.springboot.app.models.entities.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {

}
