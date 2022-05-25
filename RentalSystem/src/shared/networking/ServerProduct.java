package shared.networking;

import shared.objects.product.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerProduct extends Remote {
	void add(double price, Color color, EquipmentType equipmentType, Size size, int amount)throws
			RemoteException;
	void remove(int index)throws RemoteException;
	Product getProduct(int index)throws RemoteException;
	ProductList getAllProducts()throws RemoteException;
	void changeProduct(int index, double newPrice, Color newColor, Size newSize, int amount)throws RemoteException;
	void add(Product product)throws RemoteException;
	int getRentedAmount(int id) throws RemoteException;
}
