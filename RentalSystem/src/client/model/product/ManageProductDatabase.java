package client.model.product;

import shared.objects.product.*;

import java.sql.*;

public class ManageProductDatabase implements ManageProductsPersistence
{
    public ManageProductDatabase() throws SQLException {
        DriverManager.registerDriver(new org.postgresql.Driver());
    }

    private Connection getConnection() throws SQLException {
    String url = "jdbc:postgresql://localhost:5432/postgres?currentSchema=rentalsystem";
    String user = "postgres";
    String pw = "admin";
    Connection connection = null;
        connection = DriverManager.getConnection(url, user, pw);
    return connection;
}

    @Override
    public ProductList load() throws SQLException {
        ProductList list = new ProductList();
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Product");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
              int id = resultSet.getInt("id");
                EquipmentType type = EquipmentType.valueOf(resultSet.getString("name"));
                String sizeString = resultSet.getString("size");
                Size size = null;
                if(sizeString.contains("cm")){
                    sizeString = sizeString.substring(0, sizeString.length() - 2);
                    size = new MetricFormat(Double.parseDouble(sizeString));
                } else {
                    size = new LabelFormat(sizeString);
                }
                Color color = Color.valueOf(resultSet.getString("color"));
                double price = resultSet.getDouble("price");

                Product product = new Product(id, price, color, type, size);
                list.add(product);
            }
        } finally {
            connection.close();
        }
        return list;
    }

    @Override
    public void save(ProductList productList) throws SQLException {
        clear();
        Connection connection = getConnection();
        for (int i = 0; i< productList.size(); i++)
        {
            Product product= productList.get(i);
            try
            {
                PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO Product(id, name, size, color, price) VALUES(?, ?, ?, ?, ?);");
                executeStatement(statement, product);
            }
            finally {
                connection.close();
            }
        }

    }

    @Override
    public void save(Product product) throws SQLException {
        Connection connection = getConnection();
        try
        {
            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO Product(id, name, size, color, price) VALUES(?, ?, ?, ?, ?);");
            executeStatement(statement, product);
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void change(Product product) throws SQLException {
		Connection connection = getConnection();
		try
		{
			PreparedStatement statement =
					connection.prepareStatement("UPDATE Product SET name = ?, size = ?, color = ?, price = ? WHERE id = ?");
            statement.setString(1, product.getType().toString());
            statement.setString(2, product.getSize().toString());
            statement.setString(3, product.getColor().toString());
            statement.setDouble(4, product.getPrice());
            statement.setInt(5, product.getId());

            statement.executeUpdate();
		}
		finally {
			connection.close();
		}
    }


    @Override
    public void remove(Product product) throws SQLException {
        Connection connection = getConnection();
        try
        {
            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM Product WHERE id = ?");
            statement.setInt(1, product.getId());
            statement.executeUpdate();
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void clear() {
        //TODO: make this
    }

	private void executeStatement(PreparedStatement statement, Product product) throws SQLException {
		statement.setInt(1, product.getId());
		statement.setString(2, product.getType().toString());
		statement.setString(3, product.getSize().toString());
		statement.setString(4, product.getColor().toString());
		statement.setDouble(5, product.getPrice());

		statement.executeUpdate();
	}

}
