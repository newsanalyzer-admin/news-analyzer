package org.newsanalyzer.apitests.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.newsanalyzer.apitests.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pool.
 * Provides connection acquisition, release, and transaction management for tests.
 */
public class DatabaseConnectionManager {

    private static DatabaseConnectionManager instance;
    private final HikariDataSource dataSource;
    private final DatabaseConfig config;

    private DatabaseConnectionManager() {
        this.config = DatabaseConfig.getInstance();
        this.dataSource = createDataSource();
    }

    private DatabaseConnectionManager(DatabaseConfig config) {
        this.config = config;
        this.dataSource = createDataSource();
    }

    /**
     * Get singleton instance.
     */
    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    /**
     * Get singleton instance with custom config.
     */
    public static synchronized DatabaseConnectionManager getInstance(DatabaseConfig config) {
        if (instance == null) {
            instance = new DatabaseConnectionManager(config);
        }
        return instance;
    }

    /**
     * Reset the singleton instance and close existing connections.
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private HikariDataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        // Basic connection settings
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());

        // Pool configuration - optimized for tests
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());

        // Pool name for debugging
        hikariConfig.setPoolName("api-tests-pool");

        // Connection test query
        hikariConfig.setConnectionTestQuery("SELECT 1");

        // Auto-commit disabled for transaction control
        hikariConfig.setAutoCommit(false);

        return new HikariDataSource(hikariConfig);
    }

    /**
     * Get the underlying DataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Acquire a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Release a connection back to the pool.
     */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Begin a transaction (set auto-commit to false).
     */
    public void beginTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.setAutoCommit(false);
        }
    }

    /**
     * Commit the current transaction.
     */
    public void commit(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
        }
    }

    /**
     * Rollback the current transaction.
     */
    public void rollback(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }

    /**
     * Execute a task within a transaction, automatically handling commit/rollback.
     */
    public <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            beginTransaction(connection);
            T result = callback.execute(connection);
            commit(connection);
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw e;
        } finally {
            releaseConnection(connection);
        }
    }

    /**
     * Execute a task within a transaction (void version).
     */
    public void executeInTransaction(TransactionVoidCallback callback) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            beginTransaction(connection);
            callback.execute(connection);
            commit(connection);
        } catch (SQLException e) {
            rollback(connection);
            throw e;
        } finally {
            releaseConnection(connection);
        }
    }

    /**
     * Check if the connection pool is running.
     */
    public boolean isRunning() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Get pool statistics for debugging.
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "DataSource not initialized";
        }
        return String.format("Pool[active=%d, idle=%d, waiting=%d, total=%d]",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.getHikariPoolMXBean().getTotalConnections());
    }

    /**
     * Close the connection pool and release all resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Functional interface for transaction callbacks returning a value.
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws SQLException;
    }

    /**
     * Functional interface for transaction callbacks with no return value.
     */
    @FunctionalInterface
    public interface TransactionVoidCallback {
        void execute(Connection connection) throws SQLException;
    }
}
