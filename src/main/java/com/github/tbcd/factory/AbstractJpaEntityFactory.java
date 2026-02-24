package com.github.tbcd.factory;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.function.Consumer;

/**
 * JPA-backed implementation of {@link AbstractEntityFactory} that uses a
 * {@link JpaRepository} for persistence and retrieval.
 *
 * <p>This class provides default implementations for:</p>
 * <ul>
 *     <li>{@link #save(Object)} — delegates to {@link JpaRepository#save(Object)}</li>
 *     <li>{@link #random(int)} — retrieves existing entities from the database,
 *         creating new ones if fewer than requested exist</li>
 *     <li>{@link #randomWith(int, Consumer)} — retrieves existing entities matching
 *         the given criteria using Spring Data's {@link Example} mechanism,
 *         creating new ones if fewer than requested match</li>
 * </ul>
 *
 * <p>Subclasses only need to implement:</p>
 * <ul>
 *     <li>{@link #getClazz()} — the entity class to generate</li>
 * </ul>
 *
 * <p>The following methods can optionally be overridden to customize behavior:</p>
 * <ul>
 *     <li>{@link #getModel()} — to customize Instancio generation rules</li>
 *     <li>{@link #afterInstantiate(Object)} — to apply post-instantiation
 *         transformations before persistence</li>
 * </ul>
 *
 * @param <T> the type of entity this factory manages
 * @see AbstractEntityFactory
 * @see EntityFactory
 * @see JpaRepository
 */
public abstract class AbstractJpaEntityFactory<T> extends AbstractEntityFactory<T> {

	private final JpaRepository<T, ?> jpaRepository;

	public AbstractJpaEntityFactory(JpaRepository<T, ?> jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public T save(T entity) {
		return jpaRepository.save(entity);
	}

	@Override
	public List<T> random(int count) {
		Page<T> page = jpaRepository.findAll(Pageable.ofSize(count));
		List<T> entities = page.getContent();
		if (entities.size() < count) {
			entities.addAll(create(count - entities.size()));
		}
		return entities;
	}

	@Override
	public List<T> randomWith(int count, Consumer<T> customizer) {
		Example<T> example = createExample(customizer);
		Page<T> page = jpaRepository.findAll(example, Pageable.ofSize(count));
		List<T> entities = page.getContent();
		if (entities.size() < count) {
			entities.addAll(createWith(count - entities.size(), customizer));
		}
		return entities;
	}

	private Example<T> createExample(Consumer<T> customizer) {
		try {
			T probe = this.getClazz().getDeclaredConstructor().newInstance();
			customizer.accept(probe);
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
			return Example.of(probe, matcher);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
