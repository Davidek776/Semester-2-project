package shared.networking;

import shared.objects.reservation.Reservation;
import shared.objects.reservation.ReservationList;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerReservation extends Remote {
    void add(Reservation reservation) throws RemoteException;
    ReservationList getAll()throws RemoteException;
    Reservation getByIndex(int index)throws RemoteException;
    Reservation get(int id)throws RemoteException;
    void remove(int index)throws RemoteException;
  int getUniqueId()throws RemoteException;
}
