package shared.objects.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.objects.product.*;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ReservationListTest {

	private ReservationList list;

	@BeforeEach
	void setUp() {
		list = new ReservationList();
	}

	@Test
	void add() {
		list.add(new Reservation(0, "name", new ProductList()));
		list.add(new Reservation(1, "name", new ProductList()));
		assertEquals(2, list.size());
	}

	@Test
	void testAdd() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		assertEquals(2, list.size());
	}

	@Test
	void change() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		list.change(0, ReservationStatus.returned);
		assertEquals(ReservationStatus.returned, list.getByIndex(0).getStatus());
	}

	@Test
	void get() {
		Reservation r = new Reservation(0, "name", new ProductList());
		list.add(r);

		assertEquals(r, list.get(0));
	}

	@Test
	void getByIndex() {
		Reservation r = new Reservation(0, "name", new ProductList());
		list.add(r);

		assertEquals(r, list.getByIndex(0));
	}

	@Test
	void removeByIndex() {
		Reservation r = new Reservation(0, "name", new ProductList());
		list.add(r);

		assertEquals(r, list.removeByIndex(0));
	}

	@Test
	void size() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		list.add("name", new ProductList());

		assertEquals(3, list.size());
	}

	@Test
	void getUniqueId() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		list.add("name", new ProductList());

		assertEquals(3, list.getUniqueId());
	}

	@Test
	void testEquals() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		list.add("name", new ProductList());

		ReservationList list2 = new ReservationList();

		list2.add("name", new ProductList());
		list2.add("name", new ProductList());
		list2.add("name", new ProductList());

		assertTrue(list2.equals(list));
	}

	@Test
	void copy() {
		list.add("name", new ProductList());
		list.add("name", new ProductList());
		list.add("name", new ProductList());

		assertTrue(list.equals(list.copy()));
	}

	@Test
	void convertToStringArrayList() {
	}

	@Test
	void testToString() {
		ProductList productList = new ProductList();
		productList.add(1, Color.red, EquipmentType.ski, new MetricFormat(2));

		list.add("name", productList);

		Timestamp createdAt = new Timestamp(System.currentTimeMillis());
		Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 86400000);

		String str = String.format("id: %d  username: %s  created at: %s   expires at: %s  status: %s\n%s\n", 0, "name", createdAt, expiresAt, "rented", productList.toString());

		assertEquals(str, list.toString());
	}
}
