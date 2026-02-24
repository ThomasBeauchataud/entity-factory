package com.github.tbcd.factory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Factory interface for creating, persisting and retrieving test entities.
 *
 * <p>Provides a fluent API to generate entities populated with random values
 * using <a href="https://www.instancio.org/">Instancio</a>, optionally customize
 * them via a {@link Consumer}, and persist them into a datasource.</p>
 *
 * <p>Implementations may target different persistence backends. See
 * {@link AbstractEntityFactory} for an Instancio-based skeleton and
 * {@link AbstractJpaEntityFactory} for a JPA/Spring Data–backed implementation.</p>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * // Create a single entity with random values
 * Book book = bookFactory.create();
 *
 * // Create an entity with specific values
 * Book book = bookFactory.createWith(b -> b.setTitle("Clean Code"));
 *
 * // Retrieve an existing entity or create one if none exists
 * Book book = bookFactory.random();
 * }</pre>
 *
 * @param <T> the type of entity this factory manages
 * @see AbstractEntityFactory
 * @see AbstractJpaEntityFactory
 */
public interface EntityFactory<T> {

	/**
	 * Creates a single entity populated with random values and persists it
	 * into the datasource.
	 *
	 * @return the created and persisted entity, never {@code null}
	 */
	T create();

	/**
	 * Creates multiple entities populated with random values and persists them
	 * into the datasource.
	 *
	 * @param count the number of entities to create, must be greater than zero
	 * @return an unmodifiable list of created and persisted entities
	 */
	List<T> create(int count);

	/**
	 * Creates a single entity populated with random values, applies the given
	 * customizer to override specific fields, and persists it into the datasource.
	 *
	 * <p>Example:</p>
	 * <pre>{@code
	 * Book book = bookFactory.createWith(b -> b.setTitle("Effective Java"));
	 * }</pre>
	 *
	 * @param customizer a consumer used to modify the entity before persistence
	 * @return the created and persisted entity, never {@code null}
	 */
	T createWith(Consumer<T> customizer);

	/**
	 * Creates multiple entities populated with random values, applies the given
	 * customizer to each entity to override specific fields, and persists them
	 * into the datasource.
	 *
	 * @param count      the number of entities to create, must be greater than zero
	 * @param customizer a consumer used to modify each entity before persistence
	 * @return an unmodifiable list of created and persisted entities
	 */
	List<T> createWith(int count, Consumer<T> customizer);

	/**
	 * Returns a random entity from the datasource.
	 *
	 * <p>If no entity exists in the datasource, a new entity is created with
	 * random values, persisted, and returned.</p>
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code random(1).getFirst()}.</p>
	 *
	 * @return an existing or newly created entity, never {@code null}
	 */
	default T random() {
		return random(1).getFirst();
	}

	/**
	 * Returns random entities from the datasource.
	 *
	 * <p>If fewer than {@code count} entities exist in the datasource, additional
	 * entities are created with random values and persisted to fulfill the
	 * requested count.</p>
	 *
	 * @param count the number of entities to return, must be greater than zero
	 * @return a list of existing and/or newly created entities
	 */
	List<T> random(int count);

	/**
	 * Returns a random entity matching the given criteria from the datasource.
	 *
	 * <p>If no matching entity exists, a new entity is created with random values,
	 * the customizer is applied, and the entity is persisted and returned.</p>
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code randomWith(1, customizer).getFirst()}.</p>
	 *
	 * @param customizer a consumer describing the criteria the entity must match;
	 *                   also used to modify a newly created entity if needed
	 * @return an existing or newly created entity matching the criteria,
	 *         never {@code null}
	 */
	default T randomWith(Consumer<T> customizer) {
		return randomWith(1, customizer).getFirst();
	}

	/**
	 * Returns random entities matching the given criteria from the datasource.
	 *
	 * <p>If fewer than {@code count} matching entities exist, additional entities
	 * are created with random values, the customizer is applied to each, and they
	 * are persisted to fulfill the requested count.</p>
	 *
	 * @param count      the number of entities to return, must be greater than zero
	 * @param customizer a consumer describing the criteria the entities must match;
	 *                   also used to modify newly created entities if needed
	 * @return a list of existing and/or newly created entities matching the criteria
	 */
	List<T> randomWith(int count, Consumer<T> customizer);

	/**
	 * Persists the given entity into the datasource.
	 *
	 * @param entity the entity to persist, must not be {@code null}
	 * @return the persisted entity (may be a managed/merged instance depending
	 *         on the underlying persistence mechanism)
	 */
	T save(T entity);

}