package org.bsc.packagedelivery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class PackageDeliveryTest {

  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();

  private final InputStream standardIn = System.in;
  private final String NL = System.lineSeparator();

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(testOutputStream));
  }

  @AfterEach
  void tearDown() {
    System.setOut(standardOut);
    System.setIn(standardIn);
  }

  @Test
  void initialLoad() {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("loadExample.txt").getFile());
    String absolutePath = file.getAbsolutePath();

    PackageDelivery packageDelivery = new PackageDelivery();
    packageDelivery.loadInitialPackages(new String[] {absolutePath});
    packageDelivery.listRecords();

    Assertions.assertEquals(NL + "08801 15.960" + NL +
        "08079 5.500" + NL +
        "09300 3.200" + NL +
        "90005 2.000" + NL, testOutputStream.toString());
  }

  @Test
  void initialLoadFileDoesNotExist() {
    PackageDelivery packageDelivery = new PackageDelivery();
    packageDelivery.loadInitialPackages(new String[] {"foo.txt"});

    Assertions.assertEquals("File \"foo.txt\" not found. Program continues without initial loading from file." + NL, testOutputStream.toString());
  }

  @Test
  void inputValidation() {
    PackageDelivery packageDelivery = new PackageDelivery();
    Assertions.assertTrue(packageDelivery.isInputValid("5000 14500"));
    Assertions.assertTrue(packageDelivery.isInputValid("5 14500"));
    Assertions.assertTrue(packageDelivery.isInputValid("5.5 14500"));
    Assertions.assertTrue(packageDelivery.isInputValid("0.5 14500"));
    Assertions.assertTrue(packageDelivery.isInputValid("0.555 14500"));

    // too much digits after decimal point
    Assertions.assertFalse(packageDelivery.isInputValid("0.5555 14500"));
    // too much digits in destination code
    Assertions.assertFalse(packageDelivery.isInputValid("0.555 145000"));
    // too few digits in destination code
    Assertions.assertFalse(packageDelivery.isInputValid("0.555 1450"));
    // only one space separator allowed
    Assertions.assertFalse(packageDelivery.isInputValid("0.555  1450"));

    packageDelivery.processInput("0 14300");
    Assertions.assertEquals("Weight has to be grater than 0" + NL, testOutputStream.toString());
  }

}