package server.model.reservation;

import shared.networking.model.ManageReservations;
import shared.objects.reservation.Reservation;
import shared.objects.reservation.ReservationList;
import shared.objects.reservation.ReservationStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;

public class ManageReservationManager implements ManageReservations
{
    private ReservationList list;
    private PropertyChangeSupport changeSupport;
    private ManageReservationDatabase manageReservationDatabase;

    public ManageReservationManager()
    {
		list = new ReservationList();
        changeSupport = new PropertyChangeSupport(this);

        try {
            manageReservationDatabase = new ManageReservationDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void add(Reservation reservation) {
        list.add(reservation);
		update();
        changeSupport.firePropertyChange("reservationModified", null, list.convertToStringArrayList());
        // TODO maybe save
    }

    @Override
    public void remove(int index) {
        Reservation reservation = list.removeByIndex(index);
		update();
        changeSupport.firePropertyChange("reservationModified", null, list.convertToStringArrayList());
        try {
            manageReservationDatabase.remove(reservation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTotalPrice(int id) {
		update();
        return String.format("%.02f€", list.get(id).getProducts().getTotalPrice());
    }

    @Override public Reservation getReservationByIndex(int index)
    {
		update();
        return list.getByIndex(index);
    }

    @Override public Reservation getReservationById(int id)
    {
		update();
		return list.get(id);
    }

    @Override public ReservationList getAllReservations()
    {
		update();
        return list;
    }

    @Override
    public void changeReservation(int index, ReservationStatus newStatus) {
        Reservation reservation = list.get(index);
        reservation.setStatus(newStatus);

        try {
            manageReservationDatabase.change(reservation);
			update();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        changeSupport.firePropertyChange("reservationModified", null, list.convertToStringArrayList());
    }

	private void update() {
		try {
			list = manageReservationDatabase.load();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(name, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(name, listener);
    }
}
