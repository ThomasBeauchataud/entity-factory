package com.github.tbcd.factory;

import java.util.List;
import java.util.function.Consumer;

public interface EntityFactory<T> {

	/**
	 * Create an entity with random values then save it into the datasource
	 *
	 * @return The created and persisted entity
	 */
	T create();

	/**
	 * Create an entity with random values and updated with the given values then save it into the datasource
	 *
	 * @param customizer The values to update the created entity with
	 * @return The created and persisted entity
	 */
	T createWith(Consumer<T> customizer);

	/**
	 * Create multiple entities with random values then save them into the datasource
	 *
	 * @return The created and persisted
	 */
	List<T> many(int count);

	/**
	 * Return a random entity from the datasource
	 * If no entity exists, create an entity with random values then save it into the datasource
	 *
	 * @return The random or created entity
	 */
	T random();

	/**
	 * Return a random entity with the given values from the datasource
	 * If no entity exists, create an entity with random values and updated with the given values then save it into the datasource
	 *
	 * @return The random or created entity
	 */
	T randomWith(Consumer<T> customizer);

	/**
	 * Save the given entity to the datasource
	 *
	 * @param entity The entity to save
	 * @return The saved entity
	 */
	T save(T entity);

}
