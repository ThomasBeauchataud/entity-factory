package com.github.tbcd.factory;

import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

/**
 * Skeletal implementation of {@link EntityFactory} that generates random
 * entities using <a href="https://www.instancio.org/">Instancio</a>.
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *     <li>{@link #save(Object)} — the persistence logic</li>
 *     <li>{@link #random(int)} — retrieval of existing entities from the datasource</li>
 *     <li>{@link #randomWith(int, Consumer)} — retrieval of existing entities matching criteria</li>
 * </ul>
 *
 * <p>The following methods can optionally be overridden to customize behavior:</p>
 * <ul>
 *     <li>{@link #getClazz()} — resolved automatically via reflection;
 *         override only if the resolution fails (e.g. complex inheritance hierarchies)</li>
 *     <li>{@link #getModel()} — to customize Instancio generation rules
 *         (e.g. ignoring fields, constraining values)</li>
 *     <li>{@link #afterInstantiate(Object)} — to apply post-instantiation
 *         transformations before persistence (e.g. clearing IDs, computing
 *         derived fields)</li>
 * </ul>
 *
 * @param <T> the type of entity this factory manages
 * @see EntityFactory
 * @see AbstractJpaEntityFactory
 */
public abstract class AbstractEntityFactory<T> implements EntityFactory<T> {

	/**
	 * Returns the entity class managed by this factory.
	 *
	 * <p>The default implementation resolves the entity class automatically
	 * by inspecting the generic type parameter of the subclass hierarchy.
	 * Override this method only if the automatic resolution fails
	 * (e.g. with complex or non-standard inheritance hierarchies).</p>
	 *
	 * <p>This class is used by the default {@link #getModel()} implementation
	 * to create the Instancio model for random entity generation.</p>
	 *
	 * @return the entity class, never {@code null}
	 * @throws IllegalStateException if the entity class cannot be resolved
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		Type type = getClass().getGenericSuperclass();
		while (type instanceof Class<?> c) {
			type = c.getGenericSuperclass();
		}
		if (type instanceof ParameterizedType pt) {
			return (Class<T>) pt.getActualTypeArguments()[0];
		}
		throw new IllegalStateException("Cannot resolve entity class");
	}

	/**
	 * Returns the Instancio {@link Model} used to generate random instances
	 * of the entity.
	 *
	 * <p>Override this method to customize the generation rules, such as
	 * constraining field values, ignoring certain fields or supplying
	 * custom generators.</p>
	 *
	 * <p>The default implementation builds a model from {@link #getClazz()}
	 * with no additional customization.</p>
	 *
	 * <h4>Example</h4>
	 * <pre>{@code
	 * @Override
	 * protected Model<Book> getModel() {
	 *     return Instancio.of(super.getModel())
	 *             .ignore(field(Book::getId))
	 *             .generate(field(Book::getPrice), gen -> gen.doubles().range(1.0, 100.0))
	 *             .toModel();
	 * }
	 * }</pre>
	 *
	 * @return the Instancio model for entity generation, never {@code null}
	 * @see <a href="https://www.instancio.org/user-guide/#creating-a-model">Instancio Model documentation</a>
	 */
	protected Model<T> getModel() {
		InstancioApi<T> api = Instancio.of(getClazz());
		return api.toModel();
	}

	/**
	 * Hook called after each entity is instantiated by Instancio and before
	 * it is persisted via {@link #save(Object)}.
	 *
	 * <p>Override this method to apply transformations that cannot be expressed
	 * through the Instancio {@link Model}, such as clearing auto-generated IDs,
	 * computing derived fields or setting up relationships with other entities.</p>
	 *
	 * <p>The default implementation returns the entity unchanged.</p>
	 *
	 * <h4>Example</h4>
	 * <pre>{@code
	 * @Override
	 * protected Book afterInstantiate(Book book) {
	 *     book.setId(null); // let the database generate the ID
	 *     book.setSlug(Slugify.of(book.getTitle()));
	 *     return book;
	 * }
	 * }</pre>
	 *
	 * @param entity the newly instantiated entity, never {@code null}
	 * @return the entity to persist, never {@code null}
	 */
	protected T afterInstantiate(T entity) {
		return entity;
	}

	@Override
	public T create() {
		T instance = Instancio.of(getModel()).create();
		instance = afterInstantiate(instance);
		return save(instance);
	}

	@Override
	public List<T> create(int count) {
		return Instancio.ofList(getModel()).size(count).create().stream().map(instance -> {
			instance = afterInstantiate(instance);
			return save(instance);
		}).toList();
	}

	@Override
	public T createWith(Consumer<T> customizer) {
		T instance = this.create();
		customizer.accept(instance);
		instance = afterInstantiate(instance);
		return save(instance);
	}

	@Override
	public List<T> createWith(int count, Consumer<T> customizer) {
		return Instancio.ofList(getModel()).size(count).create().stream().map(instance -> {
			customizer.accept(instance);
			instance = afterInstantiate(instance);
			return save(instance);
		}).toList();
	}
}
