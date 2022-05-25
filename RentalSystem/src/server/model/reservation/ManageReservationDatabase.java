package server.model.reservation;

import shared.objects.product.*;
import shared.objects.reservation.Reservation;
import shared.objects.reservation.ReservationList;
import shared.objects.reservation.ReservationStatus;
import shared.objects.user.User;

import java.sql.*;
import java.util.Map;

public class ManageReservationDatabase implements ManageReservationPersistence
{
    public ManageReservationDatabase() throws SQLException {
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
    public ReservationList load() throws SQLException {
            ReservationList reservationList = new ReservationList();
            Connection connection = getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Reservation");
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String userName = resultSet.getString("UserName");
                    String status = resultSet.getString("status");
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    Timestamp expiresAt = resultSet.getTimestamp("expires_at");

                    Reservation reservation = new Reservation(id, userName, this.getProductFromReservation(id));
                    reservation.setCreateAt(createdAt);
                    reservation.setExpiresAt(expiresAt);
                    reservation.setStatus(ReservationStatus.valueOf(status));

                    reservationList.add(reservation);
                }
            } finally {
                connection.close();
            }

            return reservationList;
        }

    public ProductList getProductFromReservation(int reservationId) throws SQLException {
        ProductList list = new ProductList();
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("""
					select id, name, size, color, price, quantity, amount from product
					    join reservation_product rp on product.id = rp.productid
					where reservationid = %d
					""", reservationId));
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
                int quantity = resultSet.getInt("quantity");
                int amount = resultSet.getInt("amount");

                Product product = new Product(id, price, color, type, size, amount);
                for (int i = 0; i < quantity; i++)
                {
                    list.add(product);
                }
            }
        } finally {
            connection.close();
        }

        return list;
    }

    @Override
    public void save(ReservationList reservationList) throws SQLException {
        clear();
        Connection connection = getConnection();
        for (int i=0;i<reservationList.size();i++)
        {
            Reservation reservation = reservationList.get(i);
				try
				{
					PreparedStatement statement =
							connection.prepareStatement("INSERT INTO Reservation(id, UserName, status, created_at, expires_at) VALUES(?, ?, ?, ?, ?);");
					executeStatementReservation(statement, reservation);
				}
				finally {
					connection.close();
				}
        }
    }

    @Override
    public void save(Reservation reservation, Map<Product, Integer> map) throws SQLException {
        Connection connection = getConnection();
        try
        {
            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO Reservation(id, UserName, status, created_at, expires_at) VALUES(?, ?, ?, ?, ?);");
            executeStatementReservation(statement, reservation);

            for(Map.Entry<Product, Integer> entry : map.entrySet())
            {
                statement = connection.prepareStatement("INSERT INTO Reservation_product(reservationid, productid, quantity) VALUES(?, ?, ?);");
                executeStatementContains(statement, reservation.getId(), entry.getKey().getId(), entry.getValue());
            }

            for(Map.Entry<Product, Integer> entry : map.entrySet()) {
                statement = connection.prepareStatement("UPDATE Product SET amount_rented = amount_rented + ? WHERE id = ?;");
                executeStatementProduct(statement, entry.getValue(), entry.getKey().getId());
            }

        }
        finally {
            connection.close();
        }
    }

    @Override
    public void change(Reservation reservation) throws SQLException {
        Connection connection = getConnection();
        try
        {
            PreparedStatement statement =
                    connection.prepareStatement("UPDATE Reservation SET status = ? WHERE id = ?");
            statement.setString(1, reservation.getStatus().toString());
            statement.setInt(2, reservation.getId());
            statement.executeUpdate();
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void remove(Reservation reservation) throws SQLException {
        Connection connection = getConnection();
        try
        {
            PreparedStatement statement =connection.prepareStatement("DELETE FROM reservation_product WHERE reservationid = ?");
            statement.setInt(1, reservation.getId());
            statement.executeUpdate();

		   	statement = connection.prepareStatement("DELETE FROM Reservation WHERE id = ?");
			statement.setInt(1, reservation.getId());
			statement.executeUpdate();
        }
        finally {
            connection.close();
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public int getUniqueId() throws SQLException {
        Connection connection = getConnection();
        int id = -1;
        try {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT id\s
                    FROM reservation
                    ORDER BY id desc\s
                    LIMIT 1;""");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt("id") ;
            }

        } finally {
            connection.close();
        }

        return ++id;
    }

    private void executeStatementReservation(PreparedStatement statement, Reservation reservation) throws SQLException {
        statement.setInt(1, reservation.getId());
        statement.setString(2, reservation.getUserName());
        statement.setString(3, reservation.getStatus().toString());
        statement.setTimestamp(4, reservation.getCreatedAt());
        statement.setTimestamp(5, reservation.getExpiresAt());

        statement.executeUpdate();
    }

    private void executeStatementContains(PreparedStatement statement, int reservationId, int productId, int quantity) throws SQLException {
        statement.setInt(1, reservationId);
        statement.setInt(2, productId);
        statement.setInt(3,quantity);

        statement.executeUpdate();
    }

    private void executeStatementProduct(PreparedStatement statement, int value, int id) throws SQLException {
        statement.setInt(1, value);
        statement.setInt(2, id);

        statement.executeUpdate();
    }
}
