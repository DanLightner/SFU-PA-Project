package edu.francis.my.sfupa.SQLite.Services;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public abstract class AbstractGenericService<T, ID> implements GenericService<T, ID> {

    protected final CrudRepository<T, ID> repository;

    public AbstractGenericService(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return (List<T>) repository.findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<T> update(ID id, T entity) {
        if (repository.existsById(id)) {
            // Here you might need to set the id on the entity if required.
            return Optional.of(repository.save(entity));
        }
        return Optional.empty();
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }
}

