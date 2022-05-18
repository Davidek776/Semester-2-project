package server.model.customer;

import shared.objects.customer.Customer;
import shared.objects.customer.CustomerList;

import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageCustomerManager implements ManageCustomer{
    private CustomerList customerList;
    private PropertyChangeSupport changeSupport;
    private ManageCustomerDatabase manageCustomerDatabase;

    public ManageCustomerManager() {
        customerList = new CustomerList();
        changeSupport = new PropertyChangeSupport(this);

        try
        {
            manageCustomerDatabase = new ManageCustomerDatabase();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        try {
            customerList = manageCustomerDatabase.load();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void add(Customer customer)
    {
        customerList.add(customer);
    }

    public ArrayList<Customer> getCustomers(){
        return customerList.getCustomers();
    }

}
