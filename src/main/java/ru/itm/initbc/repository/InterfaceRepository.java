package ru.itm.initbc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.itm.initbc.entity.Interface;

@RepositoryRestResource(collectionResourceRel = "interface")
public interface InterfaceRepository extends JpaRepository<Interface, Long> {
}
