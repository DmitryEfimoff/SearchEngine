package main.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
}
