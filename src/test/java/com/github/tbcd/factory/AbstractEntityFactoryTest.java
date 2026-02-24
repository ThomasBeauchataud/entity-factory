package com.github.tbcd.factory;

import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.instancio.Select.field;

public class AbstractEntityFactoryTest {

	private final BookEntityFactory bookEntityFactory = new BookEntityFactory();
	private final BookAfterInstantiateEntityFactory bookAfterInstantiateEntityFactory = new BookAfterInstantiateEntityFactory();
	private final BookCustomModelEntityFactory bookCustomModelEntityFactory = new BookCustomModelEntityFactory();

	@Test
	public void testCreate() {
		Book book = bookEntityFactory.create();
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNotNull(book.id);
		Assertions.assertNotNull(book.name);
	}

	@Test
	public void testCreateWith() {
		String name = UUID.randomUUID().toString();
		Book book = bookEntityFactory.createWith((b) -> b.name = name);
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNotNull(book.id);
		Assertions.assertEquals(name, book.name);
	}

	@Test
	public void testCreateMany() {
		int count = 5;
		List<Book> books = bookEntityFactory.create(count);
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNotNull(book.id);
			Assertions.assertNotNull(book.name);
		});
	}

	@Test
	public void testCreateManyWith() {
		int count = 5;
		String name = UUID.randomUUID().toString();
		List<Book> books = bookEntityFactory.createWith(count, (b) -> b.name = name);
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNotNull(book.id);
			Assertions.assertEquals(name, book.name);
		});
	}

	@Test
	public void testCreateAfterInstantiate() {
		Book book = bookAfterInstantiateEntityFactory.create();
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNull(book.id);
		Assertions.assertNotNull(book.name);
	}

	@Test
	public void testCreateWithAfterInstantiate() {
		String name = UUID.randomUUID().toString();
		Book book = bookAfterInstantiateEntityFactory.createWith((b) -> {
			b.name = name;
			b.id = 10L;
		});
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNull(book.id);
		Assertions.assertEquals(name, book.name);
	}

	@Test
	public void testCreateManyAfterInstantiate() {
		int count = 5;
		List<Book> books = bookAfterInstantiateEntityFactory.create(count);
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNull(book.id);
			Assertions.assertNotNull(book.name);
		});
	}

	@Test
	public void testCreateManyWithAfterInstantiate() {
		int count = 5;
		String name = UUID.randomUUID().toString();
		List<Book> books = bookAfterInstantiateEntityFactory.createWith(count, (b) -> {
			b.name = name;
			b.id = 10L;
		});
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNull(book.id);
			Assertions.assertEquals(name, book.name);
		});
	}

	@Test
	public void testCreateCustomModel() {
		Book book = bookCustomModelEntityFactory.create();
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNull(book.name);
		Assertions.assertNotNull(book.id);
	}

	@Test
	public void testCreateWithCustomModel() {
		String name = UUID.randomUUID().toString();
		Book book = bookCustomModelEntityFactory.createWith((b) -> b.name = name);
		Assertions.assertNotNull(book);
		Assertions.assertInstanceOf(Book.class, book);
		Assertions.assertNotNull(book.id);
		Assertions.assertEquals(name, book.name);
	}

	@Test
	public void testCreateManyCustomModel() {
		int count = 5;
		List<Book> books = bookCustomModelEntityFactory.create(count);
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNotNull(book.id);
			Assertions.assertNull(book.name);
		});
	}

	@Test
	public void testCreateManyWithCustomModel() {
		int count = 5;
		String name = UUID.randomUUID().toString();
		List<Book> books = bookCustomModelEntityFactory.createWith(count, (b) -> b.name = name);
		Assertions.assertEquals(count, books.size());
		books.forEach(book -> {
			Assertions.assertNotNull(book);
			Assertions.assertInstanceOf(Book.class, book);
			Assertions.assertNotNull(book.id);
			Assertions.assertEquals(name, book.name);
		});
	}

	public static class Book {
		public Long id;
		public String name;
	}

	public static class BookEntityFactory extends AbstractEntityFactory<Book> {

		@Override
		public List<Book> random(int count) {
			return create(count);
		}

		@Override
		public List<Book> randomWith(int count, Consumer<Book> customizer) {
			return createWith(count, customizer);
		}

		@Override
		public Book save(Book entity) {
			return entity;
		}
	}

	public static class BookAfterInstantiateEntityFactory extends BookEntityFactory {

		@Override
		protected Book afterInstantiate(Book entity) {
			entity.id = null;
			return super.afterInstantiate(entity);
		}
	}

	public static class BookCustomModelEntityFactory extends BookEntityFactory {

		@Override
		protected Model<Book> getModel() {
			return Instancio.of(super.getModel())
					.ignore(field("name"))
					.toModel();
		}
	}
}
