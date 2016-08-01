package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.commons.exceptions.DataNotFoundException;

import org.junit.*;

import java.util.List;


public class RefSampleTest {
    //NOTE: This Sample test highlights the difference between using Ref<?> and using external String directly

    //--------------------------------------------------
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(Person.class);
        ObjectifyService.register(Car.class);
        ObjectifyService.register(House.class);
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
    }

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    //--------------------------------------------------

    @Entity
    class Person{
        @Id
        private String id;
        private String name;

        Person(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Entity
    class House{
        @Id
        private String id;
        @Index
        private String ownerId;
        private String location;

        House(String id, String ownerId, String location) {
            this.id = id;
            this.ownerId = ownerId;
            this.location = location;
        }
    }

    @Entity
    class Car{
        @Id
        private String id;
        @Index
        private String vin;
        @Index
        private Ref<Person> owner;

        Car(String id, String vin, Ref<Person> owner) {
            this.id = id;
            this.vin = vin;
            this.owner = owner;
        }
    }

    @Test
    public void sampleTest() throws DataNotFoundException{
        Person owner = new Person("person1", "Sam");
        DatastoreObjectifyDAL.storeObject(owner);


        House house1 = new House("house1", "person1", "LA");
        DatastoreObjectifyDAL.storeObject(house1);
        House lHouse1 = DatastoreObjectifyDAL.loadObjectById(house1.id, House.class);
        Assert.assertNotNull(lHouse1);

        House house2 = new House("house1", "person1", "LA");
        DatastoreObjectifyDAL.storeObject(house2);
        House lHouse2 = DatastoreObjectifyDAL.loadObjectById(house2.id, House.class);
        Assert.assertNotNull(lHouse2);

        Car car1 = new Car("car1", "vin1", Ref.create(owner));
        DatastoreObjectifyDAL.storeObject(car1);
        Car lCar1 = DatastoreObjectifyDAL.loadObjectById(car1.id, Car.class);
        Assert.assertNotNull(lCar1);

        Person nonExistent = new Person("person2", "Bob");
        //Don't store nonExistent in DB, directly store object which references nonExistent
        Car car2 = new Car("car2", "vin2", Ref.create(nonExistent));
        Car lCar2 = DatastoreObjectifyDAL.loadObjectById(car2.id, Car.class);
        Assert.assertNull(lCar2);       // <-----------


        Car retCar = DatastoreObjectifyDAL.loadObjectByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);
        Assert.assertNotNull(retCar);

        List<Car> carList = DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);
        Assert.assertEquals(1, carList.size());

    }

    @Test
    public void deleteTest() throws DataNotFoundException{
        Person owner = new Person("person1", "Sam");
        DatastoreObjectifyDAL.storeObject(owner);

        Car car1 = new Car("car1", "vin1", Ref.create(owner));
        DatastoreObjectifyDAL.storeObject(car1);
        Car lCar1 = DatastoreObjectifyDAL.loadObjectById(car1.id, Car.class);
        Assert.assertNotNull(lCar1);

        Car car2 = new Car("car2", "vin2", Ref.create(owner));
        DatastoreObjectifyDAL.storeObject(car2);
        Car lCar2 = DatastoreObjectifyDAL.loadObjectById(car2.id, Car.class);
        Assert.assertNotNull(lCar1);

        Car retCar = DatastoreObjectifyDAL.loadObjectByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);
        Assert.assertNotNull(retCar);

        List<Car> carList = DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);
        Assert.assertEquals(2, carList.size());

        DatastoreObjectifyDAL.deleteObjectsByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);

        List<Car> carList2 = DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ("owner", Person.class, "person1", Car.class);
        Assert.assertEquals(0, carList.size());

    }


}
