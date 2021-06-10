package org.bsc.packagedelivery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PackageDelivery {

  private static final String EXIT_COMMAND = "quit";
  private static final long LISTING_PERIOD_MS = 60 * 1000;

  private final HashMap<String, Double> records = new HashMap<>();
  private final ArrayList<String> sortedDestinationCodes = new ArrayList<>();
  private boolean alreadySorted = false;

  public static void main(String[] args) {
    PackageDelivery packageDelivery = new PackageDelivery();
    packageDelivery.startProcessing(args);
  }

  private void startProcessing(String[] args) {
    loadInitialPackages(args);
    startListing();
    startReading();
  }

  protected void loadInitialPackages(String[] args) {
    if (args != null && args.length > 0) {
      BufferedReader reader;
      try {
        reader = new BufferedReader(new FileReader(args[0]));
        String line = reader.readLine();
        while (line != null) {
          processInput(line);
          line = reader.readLine();
        }
        reader.close();
      } catch (FileNotFoundException e) {
        System.out.println("File \"" + args[0] + "\" not found. Program continues without initial loading from file.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void startReading() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      try {
        String input = reader.readLine();
        processInput(input);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  void processInput(String input) {
    if (EXIT_COMMAND.equalsIgnoreCase(input)) {
      System.exit(0);
    }
    if (!isInputValid(input)) {
      System.out.println("Invalid input format see readme.md for more informations regarding input format.");
      return;
    }
    String[] tokens = input.split(" ");
    Double weight = Double.parseDouble(tokens[0]);
    if (!(weight > 0)) {
      System.out.println("Weight has to be grater than 0");
      return;
    }
    addPackage(weight, tokens[1]);
  }

  boolean isInputValid(String input) {
    return (input != null && input.matches("^\\d+(\\.\\d{1,3})?\\040\\d{5}$"));
  }

  private void addPackage(Double weight, String destinationCode) {
    synchronized (this) {
      Double acumlatedWeight = records.get(destinationCode);
      if (acumlatedWeight != null) {
        records.put(destinationCode, acumlatedWeight + weight);
      } else {
        records.put(destinationCode, weight);
        sortedDestinationCodes.add(destinationCode);
      }
      alreadySorted = false;
    }
  }

  private void startListing() {
    Timer t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        listRecords();
      }
    }, 0, LISTING_PERIOD_MS);
  }

  void listRecords() {
    synchronized (this) {
      if (!alreadySorted) {
        sortedDestinationCodes.sort((o1, o2) -> {
          if (records.get(o1) < records.get(o2)) return 1;
          if (records.get(o1) > records.get(o2)) return -1;
          return 0;
        });
        alreadySorted = true;
      }
    }
    System.out.println();
    sortedDestinationCodes.forEach(code -> System.out.println(code + " " + String.format(java.util.Locale.US, "%.3f", records.get(code))));
  }

}
