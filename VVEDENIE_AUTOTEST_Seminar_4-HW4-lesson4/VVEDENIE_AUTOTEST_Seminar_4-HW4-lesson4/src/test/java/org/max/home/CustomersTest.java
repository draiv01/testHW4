package org.max.home;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class CustomersTest extends AbstractTest {
    @Test
    void getCustomers() throws SQLException {
        //given
        String sql = "SELECT * FROM customers";
        Statement stmt = getConnection().createStatement();
        int countTableSize = 0;

        //when
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            countTableSize++;
        }
        final Query query = getSession().createSQLQuery(sql).addEntity(CustomersEntity.class);
        //then
        Assertions.assertEquals(15, countTableSize);
        Assertions.assertEquals(15, query.list().size());
    }


    @Test

    void getCustomersWhenDistrict() throws SQLException {
        //given
        String sql = "SELECT * FROM customers WHERE district='Восточный'"; // Восточный
        Statement stmt = getConnection().createStatement();
        int countTableSize = 0;

        //when
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            countTableSize++;
        }
        final Query query = getSession().createSQLQuery("SELECT * FROM customers").addEntity(CustomersEntity.class);
        //then
        Assertions.assertEquals(5, countTableSize);
        Assertions.assertEquals(15, query.list().size());
    }


    @ParameterizedTest
    @CsvSource({"Penny, Smith", "Randy, Brown", "Oliver, Thompson"})
    void getCustomersByNameLastname(String name, String lastName) throws SQLException { // можно также по id
        //given
        String sql = "SELECT * FROM customers WHERE first_name='" + name + "'";
        Statement stmt = getConnection().createStatement();
        String nameStr = "";

        //when
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            nameStr = rs.getString(3);
        }
        //then
        Assertions.assertEquals(lastName, nameStr);
    }

    @Test
    void addNewCustomers() {
        //given
        CustomersEntity entity = new CustomersEntity(); // добавляем
        entity.setCustomerId((short) 16); // присваиваем ID
        entity.setFirstName("Наталья");
        entity.setLastName("Яхина");
        entity.setPhoneNumber("+7 904 214 0901");
        entity.setDistrict("Воронеж");
        entity.setStreet("Артамонова");
        entity.setHouse("36");
        entity.setApartment("31");

        //when
        Session session = getSession(); // выполняем транзакцию обьекта и закомитим
        session.beginTransaction();
        session.persist(entity);
        session.getTransaction().commit();
        // проверка на конкретный ID который добавили
        final Query query = getSession().createSQLQuery("SELECT * FROM customers WHERE customer_id=" + 16).addEntity(CustomersEntity.class);
        CustomersEntity creditEntity = (CustomersEntity) query.uniqueResult();
        //then
        Assertions.assertNotNull(creditEntity);
        Assertions.assertEquals("31", creditEntity.getApartment());
    }


    @ParameterizedTest // sql
    @CsvSource({"Smith", "Brown", "Thompson"})
    void addNewCustomers2(String lastname) throws SQLException {
        Session session = getSession();
        String sql = "SELECT MAX(customer_id) FROM customers";
        final Query queryWhereid = session.createSQLQuery(sql); // передаем на sql
        Integer num = (Integer) queryWhereid.uniqueResult() + 1; // именно наш num

        CustomersEntity customersEntity = new CustomersEntity();
        customersEntity.setCustomerId(num.shortValue());
        customersEntity.setFirstName(" ");
        customersEntity.setLastName(lastname);
        customersEntity.setPhoneNumber("");
        customersEntity.setDistrict("");
        customersEntity.setStreet("");
        customersEntity.setHouse("");
        customersEntity.setApartment("");

        session.beginTransaction();
        session.persist(customersEntity);
        session.getTransaction().commit();

        final Query queryWhere = session.createQuery("FROM " + "CustomersEntity" + " WHERE customer_id=" + num);
        System.out.println("executing: " + queryWhere.getQueryString());
        Optional<CustomersEntity> entity = queryWhere.uniqueResultOptional();
        Assertions.assertTrue(entity.isPresent());
        Assertions.assertEquals(lastname, entity.get().getLastName());
    }


    @Test
    void deleteCustomer() {
        //given
        final Query query = getSession()
                .createSQLQuery("SELECT * FROM customers WHERE customer_id=" + 16).addEntity(CustomersEntity.class);
        Optional<CustomersEntity> customersEntity = (Optional<CustomersEntity>) query.uniqueResultOptional();
        Assumptions.assumeTrue(customersEntity.isPresent());
        //when
        Session session = getSession();
        session.beginTransaction();
        session.delete(customersEntity.get());
        session.getTransaction().commit();
        //then
        final Query queryAfterDelete = getSession()
                .createSQLQuery("SELECT * FROM customers WHERE customer_id=" + 16).addEntity(CustomersEntity.class);
        Optional<CustomersEntity> customersEntityAfterDelete = (Optional<CustomersEntity>) queryAfterDelete.uniqueResultOptional();
        Assertions.assertFalse(customersEntityAfterDelete.isPresent());
    }
}
