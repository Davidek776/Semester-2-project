package client.model.reservation;


import shared.objects.product.*;
import shared.objects.reservation.Reservation;
import shared.objects.reservation.ReservationList;

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
            ReservationList list = new ReservationList();
            Connection connection = getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Reservation");
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String userName = resultSet.getString("UserName");
                    String status = resultSet.getString("status");
                    Date date = resultSet.getDate("created_at");

                    Reservation reservation = new Reservation(id, userName, this.getProductFromReservation());
                    list.add(reservation);
                }
            } finally {
                connection.close();
            }
            return list;
        }

    public ProductList getProductFromReservation() throws SQLException {
        ProductList list = new ProductList();
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Product WHERE id IN (SELECT reservationId FROM Reservation_Product");
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
    public void save(ReservationList reservationList) throws SQLException {
        clear();
        Connection connection = getConnection();
        for (int i=0;i<reservationList.size();i++)
        {
            Reservation reservation = reservationList.get(i);
            try
            {
                PreparedStatement statement =
                        connection.prepareStatement("INSERT INTO Reservation(id, UserName, status, created_at) VALUES(?, ?, ?, ?);");
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
                    connection.prepareStatement("INSERT INTO Reservation(id, UserName, status, created_at) VALUES(?, ?, ?, ?);");
            executeStatementReservation(statement, reservation);

            for(Map.Entry<Product, Integer> entry : map.entrySet())
            {
                statement = connection.prepareStatement("INSERT INTO Contains(reservationid, productid, username, quantity) VALUES(?, ?, ?, ?);");
                executeStatementContains(statement, reservation.getId(), entry.getKey().getId(), reservation.getUserName(), entry.getValue());
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
                    connection.prepareStatement("UPDATE Reservation SET id = ?");
            statement.setInt(1, reservation.getId());
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
            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM Reservation WHERE id = ?");
            statement.setInt(1, reservation.getId());

        }
        finally {
            connection.close();
        }
    }

    @Override
    public void clear() {

    }


    private void executeStatementReservation(PreparedStatement statement, Reservation reservation) throws SQLException {
        statement.setInt(1, reservation.getId());
        statement.setString(2, reservation.getUserName());
        statement.setString(3, reservation.getStatus().toString());
        statement.setDate(4, reservation.getCreatedAt());


        statement.executeUpdate();
    }

    private void executeStatementContains(PreparedStatement statement, int reservationId, int productId, String username, int quantity) throws SQLException {
        statement.setInt(1, reservationId);
        statement.setInt(2, productId);
        statement.setString(3, username);
        statement.setInt(4,quantity);

        statement.executeUpdate();
    }
}
