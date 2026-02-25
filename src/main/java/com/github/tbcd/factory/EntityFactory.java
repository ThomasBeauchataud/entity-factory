package com.github.tbcd.factory;

import java.util.Collection;
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
 * // Instantiate without persisting
 * Book book = bookFactory.make();
 *
 * // Instantiate with specific values without persisting
 * Book book = bookFactory.makeWith(b -> b.setTitle("Clean Code"));
 *
 * // Create and persist with random values
 * Book book = bookFactory.create();
 *
 * // Create and persist with specific values
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
	 * Instantiates a single entity populated with random values
	 * <strong>without persisting it</strong>.
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code make(1).getFirst()}.</p>
	 *
	 * @return the instantiated entity, never {@code null}
	 * @see #make(int)
	 */
	default T make() {
		return make(1).getFirst();
	}

	/**
	 * Instantiates multiple entities populated with random values
	 * <strong>without persisting them</strong>.
	 *
	 * <p>Useful for unit tests that do not require database interaction.</p>
	 *
	 * @param count the number of entities to instantiate, must be greater than zero
	 * @return an unmodifiable list of instantiated entities
	 */
	List<T> make(int count);

	/**
	 * Instantiates a single entity populated with random values, applies the
	 * given customizer to override specific fields, <strong>without persisting
	 * it</strong>.
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code makeWith(1, customizer).getFirst()}.</p>
	 *
	 * <p>Example:</p>
	 * <pre>{@code
	 * Book book = bookFactory.makeWith(b -> b.setTitle("Effective Java"));
	 * }</pre>
	 *
	 * @param customizer a consumer used to modify the entity after instantiation
	 * @return the instantiated entity, never {@code null}
	 * @see #makeWith(int, Consumer)
	 */
	default T makeWith(Consumer<T> customizer) {
		return makeWith(1, customizer).getFirst();
	}

	/**
	 * Instantiates multiple entities populated with random values, applies the
	 * given customizer to each entity to override specific fields,
	 * <strong>without persisting them</strong>.
	 *
	 * @param count      the number of entities to instantiate, must be greater than zero
	 * @param customizer a consumer used to modify each entity after instantiation
	 * @return an unmodifiable list of instantiated entities
	 */
	List<T> makeWith(int count, Consumer<T> customizer);

	/**
	 * Creates a single entity populated with random values and persists it
	 * into the datasource.
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code create(1).getFirst()}.</p>
	 *
	 * @return the created and persisted entity, never {@code null}
	 * @see #create(int)
	 */
	default T create() {
		return create(1).getFirst();
	}

	/**
	 * Creates multiple entities populated with random values and persists them
	 * into the datasource.
	 *
	 * <p>The default implementation delegates to {@link #make(int)} to instantiate
	 * the entities, then persists each one via {@link #save(Object)}.</p>
	 *
	 * @param count the number of entities to create, must be greater than zero
	 * @return an unmodifiable list of created and persisted entities
	 * @see #make(int)
	 * @see #save(Object)
	 */
	default List<T> create(int count) {
		return make(count).stream().map(this::save).toList();
	}

	/**
	 * Creates a single entity populated with random values, applies the given
	 * customizer to override specific fields, and persists it into the datasource.
	 *
	 * <p>This is a convenience method equivalent to calling
	 * {@code createWith(1, customizer).getFirst()}.</p>
	 *
	 * <p>Example:</p>
	 * <pre>{@code
	 * Book book = bookFactory.createWith(b -> b.setTitle("Effective Java"));
	 * }</pre>
	 *
	 * @param customizer a consumer used to modify the entity before persistence
	 * @return the created and persisted entity, never {@code null}
	 * @see #createWith(int, Consumer)
	 */
	default T createWith(Consumer<T> customizer) {
		return createWith(1, customizer).getFirst();
	}

	/**
	 * Creates multiple entities populated with random values, applies the given
	 * customizer to each entity to override specific fields, and persists them
	 * into the datasource.
	 *
	 * <p>The default implementation delegates to {@link #makeWith(int, Consumer)}
	 * to instantiate and customize the entities, then persists each one via
	 * {@link #save(Object)}.</p>
	 *
	 * @param count      the number of entities to create, must be greater than zero
	 * @param customizer a consumer used to modify each entity before persistence
	 * @return an unmodifiable list of created and persisted entities
	 * @see #makeWith(int, Consumer)
	 * @see #save(Object)
	 */
	default List<T> createWith(int count, Consumer<T> customizer) {
		return makeWith(count, customizer).stream().map(this::save).toList();
	}

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
	 * @see #random(int)
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
	 * @see #randomWith(int, Consumer)
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

	/**
	 * Persists the given collection of entities into the datasource.
	 *
	 * <p>The default implementation delegates to {@link #save(Object)} for each
	 * entity. Implementations may override this method to use batch persistence
	 * (e.g. {@code JpaRepository#saveAll}) for better performance.</p>
	 *
	 * @param entities the entities to persist, must not be {@code null}
	 * @return an unmodifiable list of persisted entities
	 * @see #save(Object)
	 */
	default List<T> save(Collection<T> entities) {
		return entities.stream().map(this::save).toList();
	}
}