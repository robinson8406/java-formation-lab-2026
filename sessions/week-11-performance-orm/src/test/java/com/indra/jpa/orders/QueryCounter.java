package com.indra.jpa.orders;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import java.util.function.Supplier;

/** Cuenta las sentencias SQL preparadas por Hibernate durante la ejecución de un bloque. */
public final class QueryCounter {

	private final Statistics statistics;

	public QueryCounter(EntityManagerFactory emf) {
		this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
		this.statistics.setStatisticsEnabled(true);
	}

	public <T> Measured<T> measure(Supplier<T> action) {
		statistics.clear();
		T result = action.get();
		return new Measured<>(result, statistics.getPrepareStatementCount());
	}

	public record Measured<T>(T result, long queries) {
	}
}