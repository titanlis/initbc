package ru.itm.initbc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itm.initbc.entity.SerialNumber;

@Repository
public interface SerialNumberRepository extends CrudRepository<SerialNumber, Long> {
}
