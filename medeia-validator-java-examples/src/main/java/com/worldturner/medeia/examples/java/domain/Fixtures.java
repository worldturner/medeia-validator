package com.worldturner.medeia.examples.java.domain;

public class Fixtures {
    public static Person createValidPerson() {
        Person person = new Person();
        person.setFirstName("Yon");
        person.setLastName("Yonson");
        Address address = new Address();
        address.setRegion("Wisconsin");
        address.setStreet1("1 Lumberjack Road");
        address.setCity("Townsville");
        address.setPostalCode("34216");
        address.setCountry("USA");
        person.setAddress(address);
        return person;
    }

    public static Person createInvalidPerson() {
        Person person = createValidPerson();
        person.getAddress().setStreet1(null);
        return person;
    }
}
