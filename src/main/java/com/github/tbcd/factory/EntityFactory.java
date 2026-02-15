package com.github.tbcd.factory;

import java.util.List;
import java.util.function.Consumer;

public interface EntityFactory<T> {


	T create();

	T createWith(Consumer<T> customizer);

	List<T> many(int count);

	T random();

	T save(T entity);

}
