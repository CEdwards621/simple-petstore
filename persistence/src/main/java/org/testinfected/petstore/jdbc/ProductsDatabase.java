package org.testinfected.petstore.jdbc;

import com.pyxis.petstore.domain.product.Product;
import com.pyxis.petstore.domain.product.ProductCatalog;

import java.sql.Connection;
import java.util.List;

public class ProductsDatabase implements ProductCatalog {

    private final Table productsTable;
    private final Connection connection;

    public ProductsDatabase(Table products, Connection connection) {
        this.productsTable = products;
        this.connection = connection;
    }

    public void add(Product product) {
        Insert.into(productsTable, product).execute(connection);
    }

    public Product findByNumber(String productNumber) {
        Select select = Select.from(productsTable);
        select.where("number", productNumber);
        return select.single(connection);
    }

    public List<Product> findByKeyword(String keyword) {
        Select select = Select.from(productsTable);
        select.where("lower(name) like ?");
        select.or("lower(description) like ?");
        select.addParameter(Sql.matchAnywhere(keyword));
        select.addParameter(Sql.matchAnywhere(keyword));
        return select.list(connection);
    }
}