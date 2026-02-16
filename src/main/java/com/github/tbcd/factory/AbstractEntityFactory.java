package com.github.tbcd.factory;

import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractEntityFactory<T> implements EntityFactory<T> {

	private final JpaRepository<T, ?> jpaRepository;

	public AbstractEntityFactory(JpaRepository<T, ?> jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	protected Model<T> getModel() {
		return Instancio.of(getClassz())
				.toModel();
	}

	protected abstract Class<T> getClassz();

	@Override
	public T save(T entity) {
		return jpaRepository.save(entity);
	}

	@Override
	public T create() {
		T instance = Instancio.of(getModel()).create();
		instance = afterInstantiate(instance);
		return save(instance);
	}

	@Override
	public T createWith(Consumer<T> customizer) {
		T instance = this.create();
		customizer.accept(instance);
		instance = afterInstantiate(instance);
		return save(instance);
	}

	@Override
	public List<T> many(int count) {
		return Instancio.ofList(getModel()).size(count).create().stream().map(instance -> {
			instance = afterInstantiate(instance);
			return save(instance);
		}).toList();
	}

	@Override
	public T randomWith(Consumer<T> customizer) {
		try {
			T probe = this.getClassz().getDeclaredConstructor().newInstance();
			customizer.accept(probe);
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
			Example<T> example = Example.of(probe, matcher);
			return jpaRepository.findOne(example).orElseGet(() -> this.createWith(customizer));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T random() {
		List<T> all = jpaRepository.findAll();
		return all.isEmpty() ? create() : all.getFirst();
	}

	protected T afterInstantiate(T entity) {
		return entity;
	}
}
